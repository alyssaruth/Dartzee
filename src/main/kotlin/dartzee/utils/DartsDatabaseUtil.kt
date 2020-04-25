package dartzee.utils

import dartzee.core.screen.ProgressDialog
import dartzee.core.util.DialogUtil
import dartzee.core.util.FileUtil
import dartzee.db.*
import dartzee.db.VersionEntity.Companion.insertVersion
import dartzee.logging.*
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.terminator
import org.apache.derby.jdbc.EmbeddedDriver
import java.io.File
import java.sql.DriverManager
import javax.swing.JOptionPane
import kotlin.system.exitProcess

const val TOTAL_ROUND_SCORE_SQL_STR = "(drtFirst.StartingScore - drtLast.StartingScore) + (drtLast.score * drtLast.multiplier)"

/**
 * Database helpers specific to Dartzee, e.g. first time initialisation
 */
object DartsDatabaseUtil
{
    const val MIN_DB_VERSION_FOR_CONVERSION = 7
    const val DATABASE_VERSION = 11
    const val DATABASE_NAME = "jdbc:derby:Databases/Darts;create=true"

    private val DATABASE_FILE_PATH_TEMP = DatabaseUtil.DATABASE_FILE_PATH + "_copying"

    fun getAllEntities(): MutableList<AbstractEntity<*>>
    {
        return mutableListOf(PlayerEntity(),
                DartEntity(),
                GameEntity(),
                ParticipantEntity(),
                PlayerImageEntity(),
                DartsMatchEntity(),
                AchievementEntity(),
                DartzeeRuleEntity(),
                DartzeeTemplateEntity(),
                DartzeeRoundResultEntity(),
                X01FinishEntity())
    }

    fun getAllEntitiesIncludingVersion(): MutableList<AbstractEntity<*>>
    {
        val entities = getAllEntities()
        entities.add(VersionEntity())
        return entities
    }

    fun initialiseDatabase()
    {
        DriverManager.registerDriver(EmbeddedDriver())

        DialogUtil.showLoadingDialog("Checking database status...")

        DatabaseUtil.doDuplicateInstanceCheck()

        //Pool the db connections now. Initialise with 5 to begin with?
        DatabaseUtil.initialiseConnectionPool(5)

        //Ensure this exists
        VersionEntity().createTable()
        val version = VersionEntity.retrieveCurrentDatabaseVersion()

        logger.addToContext(KEY_DB_VERSION, version?.version)

        DialogUtil.dismissLoadingDialog()

        initialiseDatabase(version)
    }

    fun initialiseDatabase(version: VersionEntity?)
    {
        if (version == null)
        {
            initDatabaseFirstTime()
            return
        }

        val versionNumber = version.version
        if (versionNumber == DATABASE_VERSION)
        {
            //nothing to do
            logger.info(CODE_DATABASE_UP_TO_DATE, "Database is up to date")
            return
        }

        if (versionNumber < MIN_DB_VERSION_FOR_CONVERSION)
        {
            val dbDetails = "Your version: $versionNumber, min supported: $MIN_DB_VERSION_FOR_CONVERSION, current: $DATABASE_VERSION"
            logger.warn(CODE_DATABASE_TOO_OLD, "Database too old, exiting. $dbDetails")
            DialogUtil.showError("Your database is too out-of-date to run this version of Dartzee. " +
                    "Please downgrade to an earlier version so that your data can be converted.\n\n$dbDetails")

            terminator.terminate(1)
        }

        logger.info(CODE_DATABASE_NEEDS_UPDATE, "Updating database to V${versionNumber + 1}")

        if (versionNumber == 7)
        {
            runSqlScriptsForVersion(8)
        }
        else if (versionNumber == 8)
        {
            DartzeeRuleEntity().createTable()
            DartzeeTemplateEntity().createTable()
            DartzeeRoundResultEntity().createTable()
        }
        else if (versionNumber == 9)
        {
            val scripts = getScripts(10).map { { runScript(10, it)} }.toTypedArray()
            runConversions(10,
                    *scripts,
                    { X01FinishConversion.convertX01Finishes() })
        }
        else if (versionNumber == 10)
        {
            runSqlScriptsForVersion(11)

            //Added "ScoringSegments"
            DartzeeRuleConversion.convertDartzeeRules()
        }

        version.version = versionNumber + 1
        version.saveToDatabase()

        logger.addToContext(KEY_DB_VERSION, version.version)
        initialiseDatabase(version)
    }

    private fun runConversions(version: Int, vararg conversions: (() -> Unit))
    {
        val t = Thread {
            val dlg = ProgressDialog.factory("Upgrading to V$version", "scripts remaining", conversions.size)
            dlg.setVisibleLater()

            conversions.forEach {
                it()
                dlg.incrementProgressLater()
            }

            dlg.disposeLater()
        }

        t.start()
        t.join()
    }
    private fun runSqlScriptsForVersion(version: Int)
    {
        val scripts = getScripts(version).map { { runScript(version, it)} }.toTypedArray()
        runConversions(version, *scripts)
    }
    private fun runScript(version: Int, scriptName: String)
    {
        val resourcePath = "/sql/v$version/"
        val rsrc = javaClass.getResource("$resourcePath$scriptName").readText()

        val batches = rsrc.split(";")

        DatabaseUtil.executeUpdates(batches)
    }
    private fun getScripts(version: Int): List<String>
    {
        return when(version)
        {
            8 -> listOf("1. Dart.sql", "2. Round.sql")
            10 -> listOf("1. DartzeeRule.sql", "2. Game.sql")
            11 -> listOf("1. Game.sql")
            else -> listOf()
        }
    }

    private fun initDatabaseFirstTime()
    {
        DialogUtil.showLoadingDialog("Initialising database, please wait...")
        logger.info(CODE_DATABASE_CREATING, "Initialising empty database")

        insertVersion()
        createAllTables()

        logger.addToContext(KEY_DB_VERSION, DATABASE_VERSION)
        logger.info(CODE_DATABASE_CREATED, "Finished creating database")
        DialogUtil.dismissLoadingDialog()
    }

    private fun createAllTables()
    {
        getAllEntities().forEach{
            it.createTable()
        }
    }

    /**
     * Backup / Restore
     */
    fun backupCurrentDatabase()
    {
        val dbFolder = File(DatabaseUtil.DATABASE_FILE_PATH)

        logger.info(CODE_STARTING_BACKUP, "About to start DB backup")

        val file = FileUtil.chooseDirectory(ScreenCache.mainScreen)
                ?: //Cancelled
                return

        val destinationPath = file.absolutePath + "\\Databases"
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

        val directoryFrom = selectAndValidateNewDatabase()
                ?: //Cancelled, or invalid
                return

        //Confirm at this point
        val confirmationQ = "Successfully conected to target database. " + "\n\nAre you sure you want to restore this database? All current data will be lost."
        val option = DialogUtil.showQuestion(confirmationQ, false)
        if (option == JOptionPane.NO_OPTION)
        {
            return
        }

        //Copy the files to a temporary file path in the application directory - Databases_copying.
        val success = directoryFrom.copyRecursively(File(DATABASE_FILE_PATH_TEMP), true)
        if (!success)
        {
            DialogUtil.showError("Restore failed - failed to copy the new database files.")
            return
        }

        //Issue a shutdown command to derby so we no longer have a handle on the old files
        val shutdown = DatabaseUtil.shutdownDerby()
        if (!shutdown)
        {
            DialogUtil.showError("Failed to shut down current database connection, unable to restore new database.")
            return
        }

        //Now switch it in
        val error = FileUtil.swapInFile(DatabaseUtil.DATABASE_FILE_PATH, DATABASE_FILE_PATH_TEMP)
        if (error != null)
        {
            DialogUtil.showError("Failed to restore database. Error: $error")
            return
        }

        DialogUtil.showInfo("Database successfully restored. Application will now exit.")
        exitProcess(0)
    }

    private fun selectAndValidateNewDatabase(): File?
    {
        DialogUtil.showInfo("Select the 'Databases' folder you want to restore from.")
        val directoryFrom = FileUtil.chooseDirectory(ScreenCache.mainScreen)
                ?: //Cancelled
                return null

        //Check it's named right
        val name = directoryFrom.name
        if (name != "Databases")
        {
            DialogUtil.showError("Selected path is not valid - you must select a folder named 'Databases'")
            return null
        }

        //Test we can connect
        val filePath = directoryFrom.absolutePath
        val testSuccess = DatabaseUtil.testConnection(filePath)
        if (!testSuccess)
        {
            DialogUtil.showError("Testing conection failed for the selected database. Cannot restore from this location.")
            return null
        }

        return directoryFrom
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
