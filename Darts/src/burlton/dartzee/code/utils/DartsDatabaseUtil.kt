package burlton.dartzee.code.utils

import burlton.core.code.util.Debug
import burlton.core.code.util.FileUtil
import burlton.dartzee.code.db.*
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.screen.ProgressDialog
import burlton.desktopcore.code.util.DialogUtil
import java.io.File
import java.util.*
import javax.swing.JOptionPane

/**
 * Database helpers specific to Dartzee, e.g. first time initialisation
 */
object DartsDatabaseUtil
{
    const val DATABASE_VERSION = 6
    const val DATABASE_NAME = "jdbc:derby:Darts;create=true"

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
                AchievementEntity())
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
        if (versionNumber == 5)
        {
            upgradeDatabaseToVersion6()

            val newVersion = VersionEntity.retrieveCurrentDatabaseVersion()!!
            newVersion.version = 6
            newVersion.saveToDatabase()

            version.version = 6
        }

        initialiseDatabase(version)
    }

    private fun runSqlScriptsForVersion(version: Int)
    {
        val resourcePath = "/sql/v$version/"
        val sqlScripts = getResourceList("/sql/v$version/")

        val t = Thread {
            val dlg = ProgressDialog.factory("Upgrading to V$version", "scripts remaining", sqlScripts.size)
            dlg.setVisibleLater()

            sqlScripts.forEach {
                val rsrc = javaClass.getResource("$resourcePath$it").readText()

                val batches = rsrc.split(";")

                DatabaseUtil.executeUpdates(batches)

                dlg.incrementProgressLater()
            }

            dlg.disposeLater()
        }

        t.start()
        t.join()

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

    private fun upgradeDatabaseToVersion6()
    {
        Debug.appendBanner("Upgrading to Version 6")

        val hmTableNameToRowCount = mutableMapOf<String, Int>()

        val t = Thread {
            val entities = getAllEntitiesIncludingVersion()
            val dlg = ProgressDialog.factory("Preparing upgrade to V6", "tables remaining", entities.size)
            dlg.setVisibleLater()

            entities.forEach {
                val name = it.getTableName()
                hmTableNameToRowCount[name] = DatabaseUtil.executeQueryAggregate("SELECT COUNT(1) FROM $name")

                createGuidTableForEntity(name)

                dlg.incrementProgressLater()
            }

            dlg.disposeLater()
        }

        t.start()
        t.join()

        runSqlScriptsForVersion(6)

        getAllEntitiesIncludingVersion().forEach {
            val name = it.getTableName()
            val newCount = DatabaseUtil.executeQueryAggregate("SELECT COUNT(1) FROM $name")

            if (newCount == hmTableNameToRowCount[name])
            {
                Debug.append("$name: $newCount rows migrated successfully")
                DatabaseUtil.dropTable("zz${name}Guids")
            }
            else
            {
                Debug.stackTrace("$name counts don't match. Migrated ${hmTableNameToRowCount[name]} -> $newCount")
            }
        }

        Debug.appendBanner("Finished DB Upgrade")
    }

    private fun createGuidTableForEntity(tableName: String)
    {
        val maxLegacyId = DatabaseUtil.executeQueryAggregate("SELECT MAX(RowId) FROM $tableName")

        val keysTable = "zz${tableName}Guids"

        DatabaseUtil.createTableIfNotExists(keysTable, "RowId INT, Guid VARCHAR(36)")

        val rowIds = (1..maxLegacyId).toList()
        val rows = rowIds.map{ id -> "($id, '${UUID.randomUUID()}')"}

        BulkInserter.insert(keysTable, rows, 5000, 50)

        DatabaseUtil.executeUpdate("CREATE INDEX ${keysTable}_RowId_Guid ON $keysTable(RowId, Guid)")
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
