package dartzee.db

import dartzee.core.util.getSqlDateNow
import dartzee.logging.CODE_BULK_SQL
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.utils.Database
import dartzee.utils.DurationTimer
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.mainDatabase
import java.sql.SQLException

object BulkInserter
{
    private var logInserts = true

    /**
     * Entity insert
     */
    fun insert(vararg entities: AbstractEntity<*>, database: Database = mainDatabase)
    {
        insert(entities.toList(), database = database)
    }
    fun insert(entities: List<AbstractEntity<*>>, rowsPerThread: Int = 5000, rowsPerStatement: Int = 100, database: Database = mainDatabase)
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

        val entitiesBatched = entities.chunked(rowsPerThread)
        val threads = entitiesBatched.map { getInsertThreadForBatch(it, tableName, rowsPerStatement, database) }

        doBulkInsert(threads, tableName, entities.size, rowsPerStatement)

        entities.forEach {it.retrievedFromDb = true}
    }
    private fun getInsertThreadForBatch(batch: List<AbstractEntity<*>>, entityName: EntityName, rowsPerInsert: Int, database: Database): Thread
    {
        return Thread {
            batch.chunked(rowsPerInsert).forEach { entities ->
                val genericInsert = "INSERT INTO $entityName VALUES ${entities.joinToString{it.getInsertBlockForStatement()}}"
                var insertQuery = genericInsert
                val conn = database.borrowConnection()

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
                            logger.logSql(insertQuery, genericInsert, timer.getDuration(), ps.updateCount, database.dbName)
                        }
                    }
                }
                catch (sqle: SQLException)
                {
                    logger.logSqlException(insertQuery, genericInsert, sqle)
                }
                finally
                {
                    database.returnConnection(conn)
                }
            }
        }
    }

    private fun doBulkInsert(threads: List<Thread>, entityName: EntityName, rowCount: Int, rowsPerStatement: Int)
    {
        if (rowCount > 100)
        {
            logInserts = false
            logger.info(CODE_BULK_SQL, "Inserting $rowCount rows into $entityName (${threads.size} threads @ $rowsPerStatement rows per insert)")
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        logInserts = true
    }
}