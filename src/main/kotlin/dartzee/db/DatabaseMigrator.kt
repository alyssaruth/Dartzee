package dartzee.db

import dartzee.logging.CODE_DATABASE_CREATED
import dartzee.logging.CODE_DATABASE_CREATING
import dartzee.logging.CODE_DATABASE_UP_TO_DATE
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings.logger

class DatabaseMigrator(migrations: Map<Int, List<(() -> Unit)>>)
{
    fun migrateToLatest(database: Database)
    {
        val version = database.getDatabaseVersion()
        if (version == null)
        {
            initDatabaseFirstTime(database)
            return
        }

        if (version == DartsDatabaseUtil.DATABASE_VERSION)
        {
            //nothing to do
            logger.info(CODE_DATABASE_UP_TO_DATE, "Database is up to date")
            return
        }
    }

    private fun initDatabaseFirstTime(database: Database)
    {
        logger.info(CODE_DATABASE_CREATING, "Initialising empty database: ${database.dbName}")

        DartsDatabaseUtil.getAllEntities(database).forEach { it.createTable() }
        database.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION)

        logger.info(CODE_DATABASE_CREATED, "Finished creating database")
    }
}