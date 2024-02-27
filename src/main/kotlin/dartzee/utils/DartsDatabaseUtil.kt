package dartzee.utils

import dartzee.core.util.DialogUtil
import dartzee.core.util.FileUtil
import dartzee.db.AbstractEntity
import dartzee.db.AchievementEntity
import dartzee.db.DartEntity
import dartzee.db.DartsMatchEntity
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.DartzeeRuleEntity
import dartzee.db.DartzeeTemplateEntity
import dartzee.db.DatabaseMigrator
import dartzee.db.DeletionAuditEntity
import dartzee.db.ForeignDatabaseValidator
import dartzee.db.GameEntity
import dartzee.db.MigrationResult
import dartzee.db.ParticipantEntity
import dartzee.db.PendingLogsEntity
import dartzee.db.PlayerEntity
import dartzee.db.PlayerImageEntity
import dartzee.db.SyncAuditEntity
import dartzee.db.TeamEntity
import dartzee.db.VersionEntity
import dartzee.db.X01FinishEntity
import dartzee.logging.CODE_BACKUP_ERROR
import dartzee.logging.CODE_RESTORE_ERROR
import dartzee.logging.CODE_STARTING_BACKUP
import dartzee.logging.CODE_STARTING_RESTORE
import dartzee.logging.KEY_DB_VERSION
import dartzee.`object`.DartsClient
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.connectionPoolSize
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.mainDatabase
import java.io.File
import java.sql.DriverManager
import javax.swing.JOptionPane
import kotlin.system.exitProcess
import org.apache.derby.jdbc.EmbeddedDriver

/** Database helpers specific to Dartzee, e.g. first time initialisation */
object DartsDatabaseUtil {
    const val DATABASE_VERSION = 23
    const val DATABASE_NAME = "Darts"
    const val OTHER_DATABASE_NAME =
        "DartsOther" // Tmp name used for restore from backup and/or sync

    fun getSyncEntities(database: Database = mainDatabase): List<AbstractEntity<*>> =
        getAllEntities(database).filter { it.includeInSync() }

    fun getAllEntities(database: Database = mainDatabase): List<AbstractEntity<*>> {
        return listOf(
            PlayerEntity(database),
            DartEntity(database),
            GameEntity(database),
            ParticipantEntity(database),
            PlayerImageEntity(database),
            DartsMatchEntity(database),
            AchievementEntity(database),
            DartzeeRuleEntity(database),
            DartzeeTemplateEntity(database),
            DartzeeRoundResultEntity(database),
            X01FinishEntity(database),
            PendingLogsEntity(database),
            SyncAuditEntity(database),
            DeletionAuditEntity(database),
            TeamEntity(database)
        )
    }

    fun getAllEntitiesIncludingVersion(database: Database = mainDatabase) =
        getAllEntities(database) + VersionEntity(database)

    fun initialiseDatabase(database: Database) {
        initialiseDerby()

        DialogUtil.showLoadingDialogOLD("Checking database status...")

        database.doDuplicateInstanceCheck()

        // Pool the db connections now. Initialise with 5 to begin with?
        database.initialiseConnectionPool(connectionPoolSize)

        val version = database.getDatabaseVersion()

        logger.addToContext(KEY_DB_VERSION, version)

        DialogUtil.dismissLoadingDialogOLD()

        val migrator = DatabaseMigrator(DatabaseMigrations.getConversionsMap())
        migrateDatabase(migrator, database)
    }

    private fun initialiseDerby() {
        DriverManager.registerDriver(EmbeddedDriver())

        val p = System.getProperties()
        p.setProperty("derby.system.home", InjectedThings.databaseDirectory)
        p.setProperty("derby.language.logStatementText", "${DartsClient.devMode}")
        p.setProperty("derby.language.logQueryPlan", "${DartsClient.devMode}")
    }

    fun migrateDatabase(migrator: DatabaseMigrator, database: Database) {
        val result = migrator.migrateToLatest(database, "Your")
        if (result == MigrationResult.TOO_OLD) {
            exitProcess(1)
        }

        logger.addToContext(KEY_DB_VERSION, DATABASE_VERSION)
    }

    /** Backup / Restore */
    fun backupCurrentDatabase() {
        logger.info(CODE_STARTING_BACKUP, "About to start DB backup")

        val file = DialogUtil.chooseDirectory(ScreenCache.mainScreen) ?: return
        val fullDestination = File("${file.absolutePath}/$DATABASE_NAME")
        val success = backupDatabaseToDestination(fullDestination)
        if (!success) {
            DialogUtil.showErrorOLD("There was a problem creating the backup.")
        } else {
            DialogUtil.showInfoOLD("Database successfully backed up to $fullDestination")
        }
    }

    private fun backupDatabaseToDestination(fullDestination: File): Boolean =
        try {
            val dbFolder = mainDatabase.getDirectory()
            dbFolder.copyRecursively(fullDestination)
        } catch (e: Exception) {
            logger.error(CODE_BACKUP_ERROR, "Caught $e trying to backup database", e)
            false
        }

    fun restoreDatabase() {
        logger.info(CODE_STARTING_RESTORE, "About to start DB restore")

        if (!checkAllGamesAreClosed()) {
            return
        }

        val directoryFrom = selectNewDatabase() ?: return

        val dbOther = Database(OTHER_DATABASE_NAME)
        try {
            directoryFrom.copyRecursively(dbOther.getDirectory(), true)
            validateAndRestoreDatabase(dbOther)
        } catch (e: Exception) {
            logger.error(CODE_RESTORE_ERROR, "Caught $e trying to restore database", e)
            DialogUtil.showErrorOLD("There was a problem restoring the database.")
        } finally {
            dbOther.shutDown()
            dbOther.getDirectory().deleteRecursively()
        }
    }

    private fun selectNewDatabase(): File? {
        DialogUtil.showInfoOLD("Select the '$DATABASE_NAME' folder you want to restore from.")
        val directoryFrom = DialogUtil.chooseDirectory(ScreenCache.mainScreen) ?: return null

        // Check it's named right
        val name = directoryFrom.name
        if (name != DATABASE_NAME) {
            DialogUtil.showErrorOLD(
                "Selected path is not valid - you must select a folder named '$DATABASE_NAME'"
            )
            return null
        }

        return directoryFrom
    }

    fun validateAndRestoreDatabase(dbOther: Database) {
        val validator =
            ForeignDatabaseValidator(DatabaseMigrator(DatabaseMigrations.getConversionsMap()))
        if (!validator.validateAndMigrateForeignDatabase(dbOther, "selected")) {
            return
        }

        // Confirm at this point
        val confirmationQ =
            "Successfully connected to target database.\n\nAre you sure you want to restore this database? All current data will be lost."
        val option = DialogUtil.showQuestionOLD(confirmationQ, false)
        if (option == JOptionPane.NO_OPTION) {
            return
        }

        if (swapInDatabase(dbOther)) {
            DialogUtil.showInfoOLD("Database restored successfully.")
        }
    }

    fun swapInDatabase(otherDatabase: Database): Boolean {
        // Now switch it in
        try {
            mainDatabase.shutDown()
            otherDatabase.shutDown()

            val error =
                FileUtil.swapInFile(mainDatabase.getDirectoryStr(), otherDatabase.getDirectoryStr())
            if (error != null) {
                DialogUtil.showErrorOLD("Failed to restore database. Error: $error")
                return false
            }
        } finally {
            mainDatabase.initialiseConnectionPool(connectionPoolSize)
            mainDatabase.localIdGenerator.clearCache()
        }

        return true
    }

    private fun checkAllGamesAreClosed(): Boolean {
        val openScreens = ScreenCache.getDartsGameScreens()
        if (openScreens.isNotEmpty()) {
            DialogUtil.showErrorOLD("You must close all open games before continuing.")
            return false
        }

        return true
    }
}
