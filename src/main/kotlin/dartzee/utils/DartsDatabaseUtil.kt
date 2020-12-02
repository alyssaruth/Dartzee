package dartzee.utils

import dartzee.`object`.DartsClient
import dartzee.core.util.DialogUtil
import dartzee.core.util.FileUtil
import dartzee.db.*
import dartzee.logging.CODE_STARTING_BACKUP
import dartzee.logging.CODE_STARTING_RESTORE
import dartzee.logging.KEY_DB_VERSION
import dartzee.screen.ScreenCache
import dartzee.sync.refreshSyncSummary
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.mainDatabase
import org.apache.derby.jdbc.EmbeddedDriver
import java.io.File
import java.sql.DriverManager
import javax.swing.JOptionPane
import kotlin.system.exitProcess

/**
 * Database helpers specific to Dartzee, e.g. first time initialisation
 */
object DartsDatabaseUtil {
    const val DATABASE_VERSION = 16
    const val DATABASE_NAME = "Darts"
    const val OTHER_DATABASE_NAME = "DartsOther" //Tmp name used for restore from backup and/or sync

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
            SyncAuditEntity(database)
        )
    }

    fun getAllEntitiesIncludingVersion(database: Database = mainDatabase) =
        getAllEntities(database) + VersionEntity(database)

    fun initialiseDatabase(database: Database)
    {
        initialiseDerby()

        DialogUtil.showLoadingDialog("Checking database status...")

        database.doDuplicateInstanceCheck()

        //Pool the db connections now. Initialise with 5 to begin with?
        database.initialiseConnectionPool(5)

        val version = database.getDatabaseVersion()

        logger.addToContext(KEY_DB_VERSION, version)

        DialogUtil.dismissLoadingDialog()

        val migrator = DatabaseMigrator(DatabaseMigrations.getConversionsMap())
        migrateDatabase(migrator, database)

        refreshSyncSummary()
    }

    private fun initialiseDerby()
    {
        DriverManager.registerDriver(EmbeddedDriver())

        val p = System.getProperties()
        p.setProperty("derby.system.home", InjectedThings.databaseDirectory)
        p.setProperty("derby.language.logStatementText", "${DartsClient.devMode}")
        p.setProperty("derby.language.logQueryPlan", "${DartsClient.devMode}")
    }

    fun migrateDatabase(migrator: DatabaseMigrator, database: Database)
    {
        val result = migrator.migrateToLatest(database, "Your")
        if (result == MigrationResult.TOO_OLD)
        {
            exitProcess(1)
        }

        logger.addToContext(KEY_DB_VERSION, DATABASE_VERSION)
    }

    /**
     * Backup / Restore
     */
    fun backupCurrentDatabase()
    {
        val dbFolder = mainDatabase.getDirectory()

        logger.info(CODE_STARTING_BACKUP, "About to start DB backup")

        val file = DialogUtil.chooseDirectory(ScreenCache.mainScreen) ?: return

        val destinationPath = "${file.absolutePath}/$DATABASE_NAME"
        val success = dbFolder.copyRecursively(File(destinationPath))
        if (!success)
        {
            DialogUtil.showError("There was a problem creating the backup.")
        }

        DialogUtil.showInfo("Database successfully backed up to $destinationPath")
    }

    fun restoreDatabase()
    {
        logger.info(CODE_STARTING_RESTORE, "About to start DB restore")

        if (!checkAllGamesAreClosed())
        {
            return
        }

        val directoryFrom = selectNewDatabase() ?: return

        val dbOther = Database(OTHER_DATABASE_NAME)
        try
        {
            directoryFrom.copyRecursively(dbOther.getDirectory(), true)
            validateAndRestoreDatabase(dbOther)
        }
        finally
        {
            dbOther.getDirectory().deleteRecursively()
        }
    }
    private fun selectNewDatabase(): File?
    {
        DialogUtil.showInfo("Select the '$DATABASE_NAME' folder you want to restore from.")
        val directoryFrom = DialogUtil.chooseDirectory(ScreenCache.mainScreen) ?: return null

        //Check it's named right
        val name = directoryFrom.name
        if (name != DATABASE_NAME)
        {
            DialogUtil.showError("Selected path is not valid - you must select a folder named '$DATABASE_NAME'")
            return null
        }

        return directoryFrom
    }
    private fun validateAndRestoreDatabase(dbOther: Database)
    {
        val validator = ForeignDatabaseValidator(DatabaseMigrator(DatabaseMigrations.getConversionsMap()))
        if (!validator.validateAndMigrateForeignDatabase(dbOther, "selected"))
        {
            return
        }

        //Confirm at this point
        val confirmationQ = "Successfully conected to target database. " + "\n\nAre you sure you want to restore this database? All current data will be lost."
        val option = DialogUtil.showQuestion(confirmationQ, false)
        if (option == JOptionPane.NO_OPTION)
        {
            return
        }

        if (swapInDatabase(dbOther))
        {
            DialogUtil.showInfo("Database restored successfully")
        }
    }

    fun swapInDatabase(otherDatabase: Database): Boolean
    {
        //Now switch it in
        try
        {
            mainDatabase.closeConnections()
            mainDatabase.shutDown()

            otherDatabase.closeConnections()
            otherDatabase.shutDown()

            val error = FileUtil.swapInFile(mainDatabase.getDirectoryStr(), otherDatabase.getDirectoryStr())
            if (error != null)
            {
                DialogUtil.showError("Failed to restore database. Error: $error")
                return false
            }
        }
        finally
        {
            mainDatabase.initialiseConnectionPool(5)
        }

        return true
    }

    private fun checkAllGamesAreClosed(): Boolean
    {
        val openScreens = ScreenCache.getDartsGameScreens()
        if (openScreens.isNotEmpty())
        {
            DialogUtil.showError("You must close all open games before continuing.")
            return false
        }

        return true
    }
}
