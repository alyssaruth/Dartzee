package burlton.dartzee.code.utils

import burlton.core.code.util.Debug
import burlton.core.code.util.FileUtil
import burlton.dartzee.code.db.*
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.util.DialogUtil
import java.io.File
import javax.swing.JOptionPane

/**
 * Database helpers specific to Dartzee, e.g. first time initialisation
 */
object DartsDatabaseUtil
{
    val DATABASE_VERSION = 6

    private val DATABASE_FILE_PATH_TEMP = DatabaseUtil.DATABASE_FILE_PATH + "_copying"

    @JvmStatic fun getAllEntities(): MutableList<AbstractEntity<*>>
    {
        return mutableListOf(PlayerEntity(),
                DartEntity(),
                GameEntity(),
                ParticipantEntity(),
                RoundEntity(),
                PlayerImageEntity(),
                DartsMatchEntity(),
                AchievementEntity(),
                DartzeeRuleEntity())
    }

    @JvmStatic fun getAllEntitiesIncludingVersion(): MutableList<AbstractEntity<*>>
    {
        val entities = getAllEntities()
        entities.add(VersionEntity())
        return entities
    }

    fun initialiseDatabase()
    {
        DialogUtil.showLoadingDialog("Checking database status...")

        DatabaseUtil.doDuplicateInstanceCheck()

        //Pool the db connections now. Initialise with 5 to begin with?
        DatabaseUtil.initialiseConnectionPool(5)

        //Ensure this exists
        VersionEntity().createTable()
        val version = VersionEntity.retrieveCurrentDatabaseVersion()

        DialogUtil.dismissLoadingDialog()

        initialiseDatabase(version)
    }

    private fun initialiseDatabase(version: VersionEntity?)
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
            Debug.append("Database versions match.")
            return
        }

        if (versionNumber == 3)
        {
            upgradeDatabaseToVersion4()
            version.version = 4
            version.saveToDatabase()
        }

        if (versionNumber == 4)
        {
            upgradeDatabaseToVersion5()
            version.version = 5
            version.saveToDatabase()
        }

        //From now on, run SQL scripts from resources/sql/v{N+1}
        if (versionNumber < DATABASE_VERSION)
        {
            val newVersion = versionNumber + 1
            runSqlScriptsForVersion(newVersion)

            version.version = newVersion
            version.saveToDatabase()
        }

        initialiseDatabase(version)
    }

    private fun runSqlScriptsForVersion(version: Int)
    {
        Debug.appendBanner("Upgrading to Version $version")

        val resourcePath = "/sql/v$version/"
        val sqlScripts = getResourceList("/sql/v$version/")
        sqlScripts.forEach{
            val rsrc = javaClass.getResource("$resourcePath$it").readText()

            val batches = rsrc.split(";")

            DatabaseUtil.executeUpdates(batches)
        }

        Debug.appendBanner("Finished upgrading database")
    }

    private fun getResourceList(path: String): List<String>
    {
        val stream = javaClass.getResourceAsStream(path) ?: return listOf()

        return stream.bufferedReader().use { it.readLines() }
    }

    private fun initDatabaseFirstTime()
    {
        DialogUtil.showLoadingDialog("Initialising database, please wait...")
        Debug.appendBanner("Initting database for the first time")

        val versionEntity = VersionEntity()
        versionEntity.assignRowId()
        versionEntity.version = DATABASE_VERSION
        versionEntity.saveToDatabase()

        Debug.append("Saved database version of $DATABASE_VERSION")

        createAllTables()

        Debug.appendBanner("Finished initting database")
        DialogUtil.dismissLoadingDialog()
    }

    private fun upgradeDatabaseToVersion5()
    {
        Debug.appendBanner("Upgrading to Version 5")

        AchievementEntity().addStringColumn("AchievementDetail", 255)

        Debug.appendBanner("Finished database upgrade")
    }

    private fun upgradeDatabaseToVersion4()
    {
        Debug.appendBanner("Upgrading to Version 4")

        //Fix default on DartsMatch
        val sql = "ALTER TABLE DartsMatch ALTER COLUMN MatchParams DROP DEFAULT"
        DatabaseUtil.executeUpdate(sql)

        //Create Achievement table
        AchievementEntity().createTable()

        Debug.appendBanner("Finished database upgrade")
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

        Debug.append("About to start DB backup")

        val file = FileUtil.chooseDirectory(ScreenCache.getMainScreen())
                ?: //Cancelled
                return

        val destinationPath = file.absolutePath + "\\Databases"
        val success = FileUtil.copyDirectoryRecursively(dbFolder, destinationPath)
        if (!success)
        {
            DialogUtil.showError("There was a problem creating the backup.")
        }

        DialogUtil.showInfo("Database successfully backed up to $destinationPath")
    }

    fun restoreDatabase()
    {
        Debug.append("About to start DB restore")

        if (!checkAllGamesAreClosed())
        {
            return
        }

        val directoryFrom = selectAndValidateNewDatabase("restore from.")
                ?: //Cancelled, or invalid
                return

        //Confirm at this point
        val confirmationQ = "Successfully conected to target database. " + "\n\nAre you sure you want to restore this database? All current data will be lost."
        val option = DialogUtil.showQuestion(confirmationQ, false)
        if (option == JOptionPane.NO_OPTION)
        {
            Debug.append("Restore cancelled.")
            return
        }

        //Copy the files to a temporary file path in the application directory - Databases_copying.
        FileUtil.deleteDirectoryIfExists(File(DATABASE_FILE_PATH_TEMP))
        val success = FileUtil.copyDirectoryRecursively(directoryFrom, DATABASE_FILE_PATH_TEMP)
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
            Debug.stackTraceSilently("Failed to swap in new database for restore: $error")
            DialogUtil.showError("Failed to restore database. Error: $error")
            return
        }

        DialogUtil.showInfo("Database successfully restored. Application will now exit.")
        System.exit(0)
    }

    private fun selectAndValidateNewDatabase(messageSuffix: String): File?
    {
        DialogUtil.showInfo("Select the 'Databases' folder you want to $messageSuffix")
        val directoryFrom = FileUtil.chooseDirectory(ScreenCache.getMainScreen())
                ?: //Cancelled
                return null

        //Check it's named right
        val name = directoryFrom.name
        if (name != "Databases")
        {
            Debug.append("Aborting - selected folder invalid: $directoryFrom")
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
        if (!openScreens.isEmpty())
        {
            Debug.append("Aborting - there are games still open.")
            DialogUtil.showError("You must close all open games before continuing.")
            return false
        }

        return true
    }
}
