package dartzee.db

import dartzee.core.util.getSqlDateNow
import dartzee.logging.CODE_BULK_SQL
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.DurationTimer
import dartzee.utils.InjectedThings.logger
import java.sql.SQLException

object BulkInserter
{
    private var logInserts = true

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

        val tableName = entities.first().getTableName()
        if (entities.any { it.retrievedFromDb })
        {
            logger.error(CODE_SQL_EXCEPTION, "Attempting to bulk insert $tableName entities, but some are already in the database")
            return
        }

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
                val genericInsert = "INSERT INTO $tableName VALUES ${entities.joinToString{it.getInsertBlockForStatement()}}"
                var insertQuery = genericInsert
                val conn = mainDatabase.borrowConnection()

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
                            logger.logSql(insertQuery, genericInsert, timer.getDuration())
                        }
                    }
                }
                catch (sqle: SQLException)
                {
                    logger.logSqlException(insertQuery, genericInsert, sqle)
                }
                finally
                {
                    mainDatabase.returnConnection(conn)
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

                mainDatabase.executeUpdate(s, logInserts)
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
            logger.info(CODE_BULK_SQL, "Inserting $rowCount rows into $tableName (${threads.size} threads @ $rowsPerStatement rows per insert)")
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        logInserts = true
    }
}