package dartzee.db

import dartzee.core.util.Debug
import dartzee.core.util.getSqlDateNow
import dartzee.logging.CODE_BULK_SQL
import dartzee.utils.DatabaseUtil
import dartzee.utils.DurationTimer
import dartzee.utils.InjectedThings.logger
import java.sql.SQLException

object BulkInserter
{
    var logInserts = true

    /**
     * Entity insert
     */
    fun insert(vararg entities: AbstractEntity<*>)
    {
        insert(entities.toList())
    }
    fun insert(entities: List<AbstractEntity<*>>, rowsPerThread: Int = 5000, rowsPerStatement: Int = 100)
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

        val threads = mutableListOf<Thread>()
        val entitiesBatched = entities.chunked(rowsPerThread)
        entitiesBatched.forEach{
            val t = getInsertThreadForBatch(it, tableName, rowsPerStatement)
            threads.add(t)
        }

        doBulkInsert(threads, tableName, entities.size, rowsPerStatement)

        entities.forEach {it.retrievedFromDb = true}
    }
    private fun getInsertThreadForBatch(batch: List<AbstractEntity<*>>, tableName: String, rowsPerInsert: Int): Thread
    {
        return Thread {
            batch.chunked(rowsPerInsert).forEach { entities ->
                var insertQuery = "INSERT INTO $tableName VALUES ${entities.joinToString{it.getInsertBlockForStatement()}}"
                val conn = DatabaseUtil.borrowConnection()

                try
                {
                    conn.prepareStatement(insertQuery).use { ps ->
                        entities.forEachIndexed { index, entity ->
                            entity.dtLastUpdate = getSqlDateNow()
                            insertQuery = entity.writeValuesToInsertStatement(insertQuery, ps, index)
                        }

                        val timer = DurationTimer()
                        ps.executeUpdate()

                        if (logInserts)
                        {
                            logger.logSql(insertQuery, ps.toString(), timer.getDuration())
                        }
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
            }
        }
    }

    /**
     * Ad-hoc insert
     */
    fun insert(tableName: String, rows: List<String>, rowsPerThread: Int, rowsPerStatement: Int)
    {
        val threads = mutableListOf<Thread>()
        rows.chunked(rowsPerThread).forEach{
            val t = getInsertThreadForBatch(tableName, it, rowsPerStatement)
            threads.add(t)
        }

        doBulkInsert(threads, tableName, rows.size, rowsPerStatement)
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

    /**
     * Generic
     */
    private fun doBulkInsert(threads: List<Thread>, tableName: String, rowCount: Int, rowsPerStatement: Int)
    {
        if (rowCount > 500)
        {
            logInserts = false
            logger.logInfo(CODE_BULK_SQL, "Inserting $rowCount rows into $tableName (${threads.size} threads @ $rowsPerStatement rows per insert)")
        }

        threads.forEach{ it.start() }
        threads.forEach{ it.join() }

        logInserts = true
    }
}