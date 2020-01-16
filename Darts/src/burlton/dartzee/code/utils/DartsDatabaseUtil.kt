package burlton.dartzee.code.utils

import burlton.core.code.util.Debug
import burlton.core.code.util.FileUtil
import burlton.dartzee.code.achievements.LAST_ROUND_FROM_PARTICIPANT
import burlton.dartzee.code.db.*
import burlton.dartzee.code.db.VersionEntity.Companion.insertVersion
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.screen.ProgressDialog
import burlton.desktopcore.code.util.DialogUtil
import java.io.File
import java.util.*
import javax.swing.JOptionPane
import kotlin.system.exitProcess

const val TOTAL_ROUND_SCORE_SQL_STR = "(drtFirst.StartingScore - drtLast.StartingScore) + (drtLast.score * drtLast.multiplier)"

/**
 * Database helpers specific to Dartzee, e.g. first time initialisation
 */
object DartsDatabaseUtil
{
    const val DATABASE_VERSION = 10
    const val DATABASE_NAME = "jdbc:derby:Darts;create=true"

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
        else if (versionNumber == 5)
        {
            upgradeDatabaseToVersion6()

            val newVersion = VersionEntity.retrieveCurrentDatabaseVersion()!!
            newVersion.version = 6
            newVersion.saveToDatabase()

            version.version = 6
        }
        else if (versionNumber == 6)
        {
            Debug.appendBanner("Upgrading to Version 7")
            DatabaseUtil.executeUpdate("CREATE INDEX ParticipantId_RoundNumber ON Round(ParticipantId, RoundNumber)")
            version.version = 7
            version.saveToDatabase()
        }
        else if (versionNumber == 7)
        {
            runSqlScriptsForVersion(8)
            version.version = 8
            version.saveToDatabase()
        }
        else if (versionNumber == 8)
        {
            Debug.appendBanner("Upgrading to Version 9")
            DartzeeRuleEntity().createTable()
            DartzeeTemplateEntity().createTable()
            DartzeeRoundResultEntity().createTable()

            version.version = 9
            version.saveToDatabase()
        }
        else if (versionNumber == 9)
        {
            runSqlScriptsForVersion(10)

            convertX01Finishes()

            version.version = 10
            version.saveToDatabase()
        }

        initialiseDatabase(version)
    }

    private fun runSqlScriptsForVersion(version: Int)
    {
        val resourcePath = "/sql/v$version/"
        val sqlScripts = getScripts(version)

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
    private fun getScripts(version: Int): List<String>
    {
        return when(version)
        {
            6 -> listOf("1. Version.sql", "2. Achievement.sql", "3. Dart.sql", "4. DartsMatch.sql", "5. Game.sql",
                    "6. Participant.sql", "7. Player.sql", "8. PlayerImage.sql", "9. Round.sql")
            8 -> listOf("1. Dart.sql", "2. Round.sql")
            10 -> listOf("1. DartzeeRule.sql", "2. Game.sql")
            else -> listOf()
        }
    }

    private fun convertX01Finishes()
    {
        X01FinishEntity().createTable()

        val zzParticipants = prepareParticipantTempTable()
        val sql = getX01FinishSql(zzParticipants)

        val finishes = mutableListOf<X01FinishEntity>()
        DatabaseUtil.executeQuery(sql).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getString("PlayerId")
                val gameId = rs.getString("GameId")
                val finish = rs.getInt("StartingScore")
                val dtFinished = rs.getTimestamp("DtFinished")

                val entity = X01FinishEntity()
                entity.assignRowId()
                entity.playerId = playerId
                entity.gameId = gameId
                entity.finish = finish
                entity.dtCreation = dtFinished

                finishes.add(entity)
            }
        }

        BulkInserter.insert(finishes)
        DatabaseUtil.dropTable(zzParticipants)
    }
    private fun prepareParticipantTempTable(): String
    {
        val zzParticipants = DatabaseUtil.createTempTable("FinishedParticipants", "PlayerId VARCHAR(36), GameId VARCHAR(36), ParticipantId VARCHAR(36), RoundNumber INT, DtFinished TIMESTAMP")
        zzParticipants ?: return ""

        val sbPt = StringBuilder()
        sbPt.append("INSERT INTO $zzParticipants ")
        sbPt.append(" SELECT p.RowId, g.RowId, pt.RowId, $LAST_ROUND_FROM_PARTICIPANT, pt.DtFinished")
        sbPt.append(" FROM Player p, Participant pt, Game g")
        sbPt.append(" WHERE pt.GameId = g.RowId")
        sbPt.append(" AND g.GameType = $GAME_TYPE_X01")
        sbPt.append(" AND pt.FinalScore > -1")
        sbPt.append(" AND pt.PlayerId = p.RowId")

        DatabaseUtil.executeUpdate(sbPt.toString())
        DatabaseUtil.executeUpdate("CREATE INDEX ${zzParticipants}_PlayerId ON $zzParticipants(PlayerId, ParticipantId, RoundNumber)")

        return zzParticipants
    }
    private fun getX01FinishSql(zzFinishedParticipants: String): String
    {
        val sb = StringBuilder()
        sb.append("SELECT zz.PlayerId, zz.GameId, drtFirst.StartingScore, zz.DtFinished")
        sb.append(" FROM Dart drtFirst, $zzFinishedParticipants zz")
        sb.append(" WHERE drtFirst.PlayerId = zz.PlayerId")
        sb.append(" AND drtFirst.ParticipantId = zz.ParticipantId")
        sb.append(" AND drtFirst.RoundNumber = zz.RoundNumber")
        sb.append(" AND drtFirst.Ordinal = 1")

        return sb.toString()
    }

    private fun initDatabaseFirstTime()
    {
        DialogUtil.showLoadingDialog("Initialising database, please wait...")
        Debug.appendBanner("Initting database for the first time")

        insertVersion()

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
            Debug.stackTraceSilently("Failed to swap in new database for restore: $error")
            DialogUtil.showError("Failed to restore database. Error: $error")
            return
        }

        DialogUtil.showInfo("Database successfully restored. Application will now exit.")
        exitProcess(0)
    }

    private fun selectAndValidateNewDatabase(messageSuffix: String): File?
    {
        DialogUtil.showInfo("Select the 'Databases' folder you want to $messageSuffix")
        val directoryFrom = FileUtil.chooseDirectory(ScreenCache.mainScreen)
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
