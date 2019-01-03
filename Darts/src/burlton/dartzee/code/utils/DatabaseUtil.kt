package burlton.dartzee.code.utils

import burlton.core.code.util.AbstractClient
import burlton.core.code.util.Debug
import burlton.desktopcore.code.util.DialogUtil
import com.sun.rowset.CachedRowSetImpl
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.sql.rowset.CachedRowSet

/**
 * Generic derby helper methods
 */
class DatabaseUtil : SqlErrorConstants
{
    companion object
    {
        private const val DATABASE_NAME = "jdbc:derby:Darts"
        private const val DATABASE_NAME_WITH_CREATE = "$DATABASE_NAME;create=true"

        @JvmField val DATABASE_FILE_PATH = System.getProperty("user.dir") + "\\Databases"

        private val hsConnections = mutableListOf<Connection>()
        private val connectionPoolLock = Any()
        private var connectionCreateCount = 0

        @JvmStatic fun initialiseConnectionPool(initialCount: Int)
        {
            synchronized(connectionPoolLock)
            {
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
            return createDatabaseConnection(DATABASE_FILE_PATH, DATABASE_NAME_WITH_CREATE)
        }

        @Throws(SQLException::class)
        private fun createDatabaseConnection(dbFilePath: String?, dbName: String): Connection
        {
            var dbFilePath = dbFilePath
            if (dbFilePath == null)
            {
                dbFilePath = DATABASE_FILE_PATH
            }

            val p = System.getProperties()
            p.setProperty("derby.system.home", dbFilePath)

            val props = Properties()
            props["user"] = "administrator"
            props["password"] = "wallace"

            return DriverManager.getConnection(dbName, props)
        }

        @JvmStatic fun executeUpdate(statement: String): Boolean
        {
            try
            {
                executeUpdateUncaught(statement)
            }
            catch (sqle: SQLException)
            {
                Debug.logSqlException(statement, sqle)
                return false
            }

            return true
        }

        @Throws(SQLException::class)
        private fun executeUpdateUncaught(statement: String)
        {
            Debug.appendSql(statement, AbstractClient.traceWriteSql)

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
        }

        @JvmStatic fun executeQuery(sb: StringBuilder): ResultSet
        {
            return executeQuery(sb.toString())
        }

        @JvmOverloads
        @JvmStatic fun executeQuery(query: String): ResultSet
        {
            val startMillis = System.currentTimeMillis()
            var crs: CachedRowSet? = null

            val conn = borrowConnection()
            try
            {
                conn.createStatement().use { s ->
                    s.executeQuery(query).use { rs ->
                        crs = CachedRowSetImpl()
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

            Debug.appendSql("(" + totalMillis + "ms) " + query, AbstractClient.traceReadSql)

            //No query should take longer than 5 seconds really...
            if (totalMillis > AbstractClient.SQL_TOLERANCE_QUERY)
            {
                Debug.stackTraceNoError("SQL query took longer than " + AbstractClient.SQL_TOLERANCE_QUERY + " millis: " + query)
            }

            //Return an empty one if something's gone wrong
            return crs ?: CachedRowSetImpl()
        }

        @JvmStatic fun executeQueryAggregate(sb: StringBuilder): Int
        {
            return executeQueryAggregate(sb.toString())
        }

        @JvmStatic fun executeQueryAggregate(sql: String): Int
        {
            try
            {
                executeQuery(sql).use { rs ->
                    rs.next()
                    return rs.getInt(1)
                }
            }
            catch (sqle: SQLException)
            {
                Debug.logSqlException(sql, sqle)
                return -1
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
                if (state == SqlErrorConstants.TABLE_ALREADY_EXISTS)
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

        @JvmStatic fun dropTable(tableName: String): Boolean
        {
            val sql = "DROP TABLE $tableName"
            return executeUpdate(sql)
        }

        @JvmStatic fun testConnection(dbPath: String): Boolean
        {
            try
            {
                createDatabaseConnection(dbPath, DATABASE_NAME_WITH_CREATE)
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
                createDatabaseConnection(DATABASE_FILE_PATH, "jdbc:derby:;shutdown=true")
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
    }
}
