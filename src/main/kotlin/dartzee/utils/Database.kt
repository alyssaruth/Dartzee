package dartzee.utils

import dartzee.core.util.DialogUtil
import dartzee.db.LocalIdGenerator
import dartzee.db.VersionEntity
import dartzee.logging.*
import dartzee.logging.exceptions.WrappedSqlException
import dartzee.utils.InjectedThings.databaseDirectory
import dartzee.utils.InjectedThings.logger
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.sql.rowset.CachedRowSet
import javax.sql.rowset.RowSetProvider
import kotlin.system.exitProcess

const val TABLE_ALREADY_EXISTS = "X0Y32"
val DATABASE_FILE_PATH: String = "${System.getProperty("user.dir")}/Databases"

/**
 * Generic derby helper methods
 */
class Database(val dbName: String = DartsDatabaseUtil.DATABASE_NAME, private val inMemory: Boolean = false)
{
    val localIdGenerator = LocalIdGenerator(this)

    private val hsConnections = mutableListOf<Connection>()
    private val connectionPoolLock = Any()
    private var connectionCreateCount = 0

    fun initialiseConnectionPool(initialCount: Int)
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

    fun borrowConnection() : Connection
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

    fun returnConnection(connection: Connection)
    {
        synchronized(connectionPoolLock)
        {
            hsConnections.add(connection)
        }
    }

    private fun createDatabaseConnection(): Connection
    {
        connectionCreateCount++

        val connection = DriverManager.getConnection(getDbStringForNewConnection(), getProps())
        logger.info(CODE_NEW_CONNECTION, "Created new connection. Total created: $connectionCreateCount, pool size: ${hsConnections.size}")
        return connection
    }

    private fun getDbStringForNewConnection() = "${getQualifiedDbName()};create=true"
    fun getQualifiedDbName() =
        if (inMemory)
        {
            "jdbc:derby:memory:Databases/$dbName"
        }
        else
        {
            "jdbc:derby:Databases/$dbName"
        }

    private fun getProps(): Properties
    {
        val props = Properties()
        props["user"] = "administrator"
        props["password"] = "wallace"
        return props
    }

    fun getDirectoryStr() = "$databaseDirectory/$dbName"
    fun getDirectory() = File(getDirectoryStr())

    fun executeUpdates(statements: List<String>): Boolean
    {
        statements.forEach {
            if (!executeUpdate(it))
            {
                return false
            }
        }

        return true
    }

    fun executeUpdate(sb: StringBuilder) = executeUpdate(sb.toString())
    fun executeUpdate(statement: String, log: Boolean = true): Boolean
    {
        try
        {
            executeUpdateUncaught(statement, log)
        }
        catch (sqle: SQLException)
        {
            logger.logSqlException(statement, "", sqle)
            return false
        }

        return true
    }

    private fun executeUpdateUncaught(statement: String, log: Boolean = true)
    {
        val timer = DurationTimer()
        val conn = borrowConnection()
        var updateCount = 0
        try
        {
            conn.createStatement().use {
                s -> s.execute(statement)
                updateCount = s.updateCount
            }
        }
        finally
        {
            returnConnection(conn)
        }

        if (log)
        {
            logger.logSql(statement, "", timer.getDuration(), updateCount, dbName)
        }
    }

    fun executeQuery(sb: StringBuilder): ResultSet
    {
        return executeQuery(sb.toString())
    }

    fun executeQuery(query: String): ResultSet
    {
        val timer = DurationTimer()
        val conn = borrowConnection()

        try
        {
            conn.createStatement().use { s ->
                val resultSet: CachedRowSet = s.executeQuery(query).use { rs ->
                    val crs = RowSetProvider.newFactory().createCachedRowSet()
                    crs.populate(rs)
                    crs
                }

                logger.logSql(query, "", timer.getDuration(), resultSet.size(), dbName)
                return resultSet
            }
        }
        catch (sqle: SQLException)
        {
            throw WrappedSqlException(query, "", sqle)
        }
        finally
        {
            returnConnection(conn)
        }
    }

    fun executeQueryAggregate(sb: StringBuilder): Int
    {
        return executeQueryAggregate(sb.toString())
    }

    fun executeQueryAggregate(sql: String): Int
    {
        executeQuery(sql).use { rs ->
            return if (rs.next()) rs.getInt(1) else -1
        }
    }

    fun doDuplicateInstanceCheck()
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
                logger.warn(CODE_DATABASE_IN_USE, "Failed multiple instance check, exiting.")
                DialogUtil.showError("Database already in use - Dartzee will now exit.")
                exitProcess(1)
            }
            else
            {
                logger.logSqlException("", "", sqle)
            }
        }

    }

    fun createTableIfNotExists(tableName: String, columnSql: String): Boolean
    {
        val statement = "CREATE TABLE $tableName($columnSql)"

        try
        {
            executeUpdateUncaught(statement)
            logger.info(CODE_TABLE_CREATED, "Created $tableName")
        }
        catch (sqle: SQLException)
        {
            val state = sqle.sqlState
            if (state == TABLE_ALREADY_EXISTS)
            {
                logger.info(CODE_TABLE_EXISTS, "$tableName already exists")
            }
            else
            {
                logger.logSqlException(statement, "", sqle)
            }

            return false
        }

        return true
    }

    fun getDatabaseVersion(): Int? = getVersionRow()?.version

    fun updateDatabaseVersion(version: Int)
    {
        val entity = getVersionRow() ?: VersionEntity(this).also { it.assignRowId() }
        entity.version = version
        entity.saveToDatabase()
    }

    fun generateLocalId(tableName: String) = localIdGenerator.generateLocalId(tableName)

    private fun getVersionRow(): VersionEntity?
    {
        val versionEntity = VersionEntity(this)
        versionEntity.createTable()
        return versionEntity.retrieveEntity("1 = 1")
    }

    fun createTempTable(tableName: String, colStr: String): String?
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

    fun dropTable(tableName: String?): Boolean
    {
        val sql = "DROP TABLE $tableName"
        return executeUpdate(sql)
    }

    fun testConnection(): Boolean
    {
        try
        {
            createDatabaseConnection()
        }
        catch (t: Throwable)
        {
            logger.error(CODE_TEST_CONNECTION_ERROR, "Failed to establish test connection for path ${getDirectory()}", t)
            return false
        }

        return true
    }

    fun shutDown(): Boolean
    {
        closeConnections()
        val command = "${getQualifiedDbName()};shutdown=true"

        try
        {
            DriverManager.getConnection(command, getProps())
        }
        catch (sqle: SQLException)
        {
            val msg = sqle.message ?: ""
            if (msg.contains("shutdown") || msg.contains("not found"))
            {
                return true
            }

            if (!msg.contains("not found"))
            {
                logger.logSqlException(command, command, sqle)
            }
        }

        return false
    }

    private fun closeConnections()
    {
        hsConnections.forEach { it.close() }
        hsConnections.clear()
    }

    fun deleteRowsFromTable(tableName: String, rowIds: List<String>): Boolean
    {
        var success = true
        rowIds.chunked(50).forEach {
            val idStr = it.joinToString{rowId -> "'$rowId'"}
            val sql = "DELETE FROM $tableName WHERE RowId IN ($idStr)"
            success = executeUpdate(sql)
        }

        return success
    }

    fun dropUnexpectedTables(): List<String>
    {
        val entities = DartsDatabaseUtil.getAllEntitiesIncludingVersion()
        val tableNameSql = entities.joinToString{ "'${it.getTableNameUpperCase()}'"}

        val sb = StringBuilder()
        sb.append(" SELECT TableName")
        sb.append(" FROM sys.systables")
        sb.append(" WHERE TableType = 'T'")
        sb.append(" AND TableName NOT IN ($tableNameSql)")

        val list = mutableListOf<String>()
        executeQuery(sb).use{ rs ->
            while (rs.next())
            {
                list.add(rs.getString("TableName"))
            }
        }

        list.forEach{ dropTable(it) }
        return list
    }
}
