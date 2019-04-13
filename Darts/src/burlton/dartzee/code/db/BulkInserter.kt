package burlton.dartzee.code.db

import burlton.core.code.util.AbstractClient
import burlton.core.code.util.Debug
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.util.getSqlDateNow
import java.sql.SQLException

object BulkInserter
{
    fun insert(vararg entities: AbstractEntity<*>)
    {
        insert(entities.toList())
    }
    @JvmStatic fun insert(entities: List<AbstractEntity<*>>)
    {
        if (entities.isEmpty())
        {
            return
        }

        if (entities.any{it.retrievedFromDb})
        {
            Debug.stackTrace("Attempting to bulk insert entities, but some are already in the database")
            return
        }

        val tableName = entities.first().getTableName()
        var insertQuery = "INSERT INTO $tableName VALUES ${entities.joinToString{it.getInsertBlockForStatement()}}"
        val conn = DatabaseUtil.borrowConnection()

        try
        {
            conn.prepareStatement(insertQuery).use { ps ->
                entities.forEachIndexed { index, entity ->
                    entity.dtLastUpdate = getSqlDateNow()
                    insertQuery = entity.writeValuesToInsertStatement(insertQuery, ps, index)
                }

                Debug.appendSql(insertQuery, AbstractClient.traceWriteSql)

                ps.executeUpdate()
            }
        }
        catch (sqle: SQLException)
        {
            Debug.logSqlException(insertQuery, sqle)
        }
        finally
        {
            DatabaseUtil.returnConnection(conn)
        }

        entities.forEach {it.retrievedFromDb = true}
    }

    fun insert(tableName: String, rows: List<String>, rowsPerThread: Int, rowsPerStatement: Int)
    {
        val traceWriteSql = AbstractClient.traceWriteSql

        val threads = mutableListOf<Thread>()
        rows.chunked(rowsPerThread).forEach{
            val t = getInsertThreadForBatch(tableName, it, rowsPerStatement)
            threads.add(t)
        }

        if (rows.size > 500)
        {
            AbstractClient.traceWriteSql = false
            Debug.append("[SQL] Inserting ${rows.size} rows into $tableName (${threads.size} threads @ $rowsPerStatement rows per insert)")
        }

        threads.forEach{ it.start() }
        threads.forEach{ it.join() }

        AbstractClient.traceWriteSql = traceWriteSql
    }
    private fun getInsertThreadForBatch(tableName: String, batch: List<String>, rowsPerStatement: Int): Thread
    {
        return Thread {
            batch.chunked(rowsPerStatement).forEach {
                var s = "INSERT INTO $tableName VALUES "

                it.forEachIndexed{index, rowValues ->
                    if (index > 0) {
                        s += ", "
                    }

                    s += rowValues
                }

                DatabaseUtil.executeUpdate(s)
            }
        }
    }
}