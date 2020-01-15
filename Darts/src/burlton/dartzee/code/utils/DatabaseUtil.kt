package burlton.dartzee.code.utils

import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.DartsClient
import burlton.desktopcore.code.util.DialogUtil
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.sql.rowset.CachedRowSet
import javax.sql.rowset.RowSetProvider

const val TABLE_ALREADY_EXISTS = "X0Y32"

/**
 * Generic derby helper methods
 */
class DatabaseUtil
{
    companion object
    {
        @JvmField val DATABASE_FILE_PATH = System.getProperty("user.dir") + "\\Databases"

        private val hsConnections = mutableListOf<Connection>()
        private val connectionPoolLock = Any()
        private var connectionCreateCount = 0

        @JvmStatic fun initialiseConnectionPool(initialCount: Int)
        {
            synchronized(connectionPoolLock)
            {
                hsConnections.clear()
                for (i in 0 until initialCount)
                {
                    val conn = createDatabaseConnection()
                    hsConnections.add(conn)
                }
            }
        }

        @JvmStatic fun borrowConnection() : Connection
        {
            synchronized(connectionPoolLock)
            {
                if (hsConnections.isEmpty())
                {
                    return createDatabaseConnection()
                }

                return hsConnections.removeAt(0)
            }
        }

        @JvmStatic fun returnConnection(connection: Connection)
        {
            synchronized(connectionPoolLock)
            {
                hsConnections.add(connection)
            }
        }

        @Throws(SQLException::class)
        @JvmStatic fun createDatabaseConnection(): Connection
        {
            connectionCreateCount++

            Debug.appendBanner("CREATED new connection. Total created: $connectionCreateCount, pool size: ${hsConnections.size}")
            return createDatabaseConnection(dbName = DartsClient.derbyDbName)
        }

        @Throws(SQLException::class)
        private fun createDatabaseConnection(dbFilePath: String = DATABASE_FILE_PATH, dbName: String): Connection
        {
            val p = System.getProperties()
            p.setProperty("derby.system.home", dbFilePath)
            p.setProperty("derby.language.logStatementText", "${DartsClient.devMode}")
            p.setProperty("derby.language.logQueryPlan", "${DartsClient.devMode}")

            val props = Properties()
            props["user"] = "administrator"
            props["password"] = "wallace"

            return DriverManager.getConnection(dbName, props)
        }

        fun executeUpdates(statements: List<String>): Boolean
        {
            val sql = getCombinedSqlForLogging(statements)
            Debug.appendSql(sql, DartsClient.traceWriteSql)

            statements.forEach{
                if (!executeUpdate(it, false))
                {
                    return false
                }
            }

            return true
        }
        private fun getCombinedSqlForLogging(batches: List<String>): String
        {
            var s = ""

            batches.forEach{
                s += "\n$it;"
            }

            return s
        }

        @JvmStatic @JvmOverloads fun executeUpdate(statement: String, log: Boolean = true): Boolean
        {
            try
            {
                executeUpdateUncaught(statement, log)
            }
            catch (sqle: SQLException)
            {
                Debug.logSqlException(statement, sqle)
                return false
            }

            return true
        }

        @Throws(SQLException::class)
        private fun executeUpdateUncaught(statement: String, log: Boolean = true)
        {
            val startMillis = System.currentTimeMillis()
            val conn = borrowConnection()
            try
            {
                conn.createStatement().use{
                    s -> s.execute(statement)
                }
            }
            finally
            {
                returnConnection(conn)
            }

            val totalMillis = System.currentTimeMillis() - startMillis
            Debug.appendSql("(${totalMillis}ms) $statement", DartsClient.traceWriteSql && log)

            if (totalMillis > DartsClient.sqlMaxDuration && !DartsClient.devMode)
            {
                Debug.stackTrace(message = "SQL update took longer than ${DartsClient.sqlMaxDuration} millis: $statement", suppressError = true)
            }
        }

        @JvmStatic fun executeQuery(sb: StringBuilder): ResultSet
        {
            return executeQuery(sb.toString())
        }

        @JvmStatic fun executeQuery(query: String): ResultSet
        {
            val startMillis = System.currentTimeMillis()
            var crs: CachedRowSet? = null

            val conn = borrowConnection()
            try
            {
                conn.createStatement().use { s ->
                    s.executeQuery(query).use { rs ->
                        crs = RowSetProvider.newFactory().createCachedRowSet()
                        crs!!.populate(rs)
                    }
                }
            }
            catch (sqle: SQLException)
            {
                Debug.logSqlException(query, sqle)
            }
            finally
            {
                returnConnection(conn)
            }

            val totalMillis = System.currentTimeMillis() - startMillis
            Debug.appendSql("(" + totalMillis + "ms) " + query, DartsClient.traceReadSql)

            //No query should take longer than 5 seconds really...
            if (totalMillis > DartsClient.sqlMaxDuration && !DartsClient.devMode)
            {
                Debug.stackTrace(message = "SQL query took longer than ${DartsClient.sqlMaxDuration} millis: $query", suppressError = true)
            }

            //Return an empty one if something's gone wrong
            return crs ?: RowSetProvider.newFactory().createCachedRowSet()
        }

        @JvmStatic fun executeQueryAggregate(sb: StringBuilder): Int
        {
            return executeQueryAggregate(sb.toString())
        }

        @JvmStatic fun executeQueryAggregate(sql: String): Int
        {
            executeQuery(sql).use { rs ->
                return if (rs.next()) rs.getInt(1) else -1
            }
        }

        @JvmStatic fun doDuplicateInstanceCheck()
        {
            try
            {
                createDatabaseConnection()
            }
            catch (sqle: SQLException)
            {
                val next = sqle.nextException
                if (next != null
                 && next.message!!.contains("Another instance of Derby may have already booted the database"))
                {
                    Debug.stackTraceSilently(sqle)
                    DialogUtil.showError("Database already in use - Dartzee will now exit.")
                    System.exit(1)
                }
                else
                {
                    Debug.stackTrace(sqle)
                }
            }

        }

        @JvmStatic fun createTableIfNotExists(tableName: String, columnSql: String): Boolean
        {
            val statement = "CREATE TABLE $tableName($columnSql)"

            try
            {
                DatabaseUtil.executeUpdateUncaught(statement)
                Debug.append("Created $tableName table.")
            }
            catch (sqle: SQLException)
            {
                val state = sqle.sqlState
                if (state == TABLE_ALREADY_EXISTS)
                {
                    Debug.append("$tableName table already exists")
                }
                else
                {
                    Debug.logSqlException(statement, sqle)
                }

                return false
            }

            return true
        }

        @JvmStatic fun createTempTable(tableName: String, colStr: String): String?
        {
            val millis = System.currentTimeMillis()
            val fullTableName = "zzTmp_$tableName$millis"

            val success = createTableIfNotExists(fullTableName, colStr)
            return if (success)
            {
                fullTableName
            }
            else null
        }

        @JvmStatic fun dropTable(tableName: String?): Boolean
        {
            val sql = "DROP TABLE $tableName"
            return executeUpdate(sql)
        }

        @JvmStatic fun testConnection(dbPath: String): Boolean
        {
            try
            {
                createDatabaseConnection(dbPath, DartsClient.derbyDbName)
            }
            catch (t: Throwable)
            {
                Debug.append("Failed to establish test connection for path $dbPath")
                Debug.stackTraceSilently(t)
                return false
            }

            Debug.append("Successfully created test connection to $dbPath")
            return true
        }

        @JvmStatic fun shutdownDerby(): Boolean
        {
            try
            {
                createDatabaseConnection(dbName = "jdbc:derby:;shutdown=true")
            }
            catch (sqle: SQLException)
            {
                val msg = sqle.message ?: ""
                if (msg.contains("shutdown"))
                {
                    //Derby ALWAYS throws an exception on shutdown.
                    return true
                }

                Debug.stackTrace(sqle)
            }

            return false
        }

        fun deleteRowsFromTable(tableName: String, rowIds: List<String>): Boolean
        {
            var success = true
            rowIds.chunked(50).forEach {
                val idStr = it.joinToString{rowId -> "'$rowId'"}
                val sql = "DELETE FROM $tableName WHERE RowId IN ($idStr)"
                success = DatabaseUtil.executeUpdate(sql)
            }

            return success
        }
    }
}
