package dartzee.db

import dartzee.core.screen.ProgressDialog
import dartzee.core.util.DialogUtil
import dartzee.logging.CODE_DATABASE_CREATED
import dartzee.logging.CODE_DATABASE_CREATING
import dartzee.logging.CODE_DATABASE_NEEDS_UPDATE
import dartzee.logging.CODE_DATABASE_TOO_OLD
import dartzee.logging.CODE_DATABASE_UP_TO_DATE
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings.logger

enum class MigrationResult {
    SUCCESS,
    TOO_OLD
}

class DatabaseMigrator(private val migrations: Map<Int, List<((database: Database) -> Any)>>) {
    fun migrateToLatest(database: Database, databaseDesc: String): MigrationResult {
        val version = database.getDatabaseVersion()
        if (version == null) {
            initDatabaseFirstTime(database)
            return MigrationResult.SUCCESS
        }

        if (version == DartsDatabaseUtil.DATABASE_VERSION) {
            // nothing to do
            logger.info(CODE_DATABASE_UP_TO_DATE, "$databaseDesc database is up to date")
            return MigrationResult.SUCCESS
        }

        val minSupported = getMinimumSupportedVersion()
        if (version < minSupported) {
            val dbDetails =
                "$databaseDesc version: $version, min supported: ${minSupported}, current: ${DartsDatabaseUtil.DATABASE_VERSION}"
            logger.warn(
                CODE_DATABASE_TOO_OLD,
                "$databaseDesc database too old, exiting. $dbDetails"
            )
            DialogUtil.showErrorOLD(
                "$databaseDesc database is too out-of-date to be upgraded by this version of Dartzee. " +
                    "Please downgrade to an earlier version so that the data can be converted.\n\n$dbDetails"
            )

            return MigrationResult.TOO_OLD
        }

        for (currentVersion in version until DartsDatabaseUtil.DATABASE_VERSION) {
            runMigrationsForVersion(database, databaseDesc, currentVersion)
        }

        return MigrationResult.SUCCESS
    }

    private fun getMinimumSupportedVersion() =
        migrations.keys.minOrNull() ?: DartsDatabaseUtil.DATABASE_VERSION

    private fun runMigrationsForVersion(database: Database, databaseDesc: String, version: Int) {
        val newVersion = version + 1
        val migrationBatch = migrations.getValue(version)
        val migrationCount = migrationBatch.size

        val updateMessage = "Upgrading $databaseDesc database to V$newVersion"

        logger.info(CODE_DATABASE_NEEDS_UPDATE, "$updateMessage ($migrationCount migrations)")

        val t = Thread {
            val dlg = ProgressDialog.factory(updateMessage, "scripts remaining", migrationCount)
            dlg.setVisibleLater()

            migrationBatch.forEach {
                it(database)
                dlg.incrementProgressLater()
            }

            database.updateDatabaseVersion(newVersion)
            dlg.disposeLater()
        }

        t.start()
        t.join()
    }

    private fun initDatabaseFirstTime(database: Database) {
        logger.info(CODE_DATABASE_CREATING, "Initialising empty database: ${database.dbName}")

        DartsDatabaseUtil.getAllEntities(database).forEach { it.createTable() }
        database.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION)

        logger.info(CODE_DATABASE_CREATED, "Finished creating database")
    }
}
