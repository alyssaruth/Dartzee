package dartzee.sync

import dartzee.core.util.DialogUtil
import dartzee.db.DatabaseMerger
import dartzee.db.DatabaseMigrator
import dartzee.db.SyncAuditEntity
import dartzee.main.promptForNonEmptyInput
import dartzee.screen.ScreenCache
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings.mainDatabase
import java.io.File
import java.io.InterruptedIOException
import java.net.SocketException
import kotlin.system.exitProcess

class SyncManager(private val dbStore: IRemoteDatabaseStore)
{
    fun doSync()
    {
        if (!validateSync())
        {
            return
        }

        val (syncMode, remoteName) = getSyncConfig() ?: return

        try
        {
            if (syncMode == SyncMode.CREATE_REMOTE)
            {
                SyncAuditEntity.insertSyncAudit(mainDatabase, remoteName)
                dbStore.pushDatabase(remoteName, mainDatabase)
            }
            else if (syncMode == SyncMode.OVERWRITE_LOCAL)
            {
                val remote = dbStore.fetchDatabase(remoteName)
                SyncAuditEntity.insertSyncAudit(remote, remoteName)
                DartsDatabaseUtil.swapInDatabase(File(remote.filePath))
            }
            else
            {
                val remoteDatabase = dbStore.fetchDatabase(remoteName)
                val merger = makeDatabaseMerger(remoteDatabase, remoteName)
                if (!merger.validateMerge())
                {
                    return
                }

                val resultingDatabase = merger.performMerge()
                dbStore.pushDatabase(remoteName, resultingDatabase)
                DartsDatabaseUtil.swapInDatabase(File(resultingDatabase.filePath))
            }

            saveRemoteName(remoteName)
            DialogUtil.showInfo("Sync completed successfully. Dartzee will now exit.")
            exitProcess(0)
        }
        catch (e: Exception)
        {
            when (e)
            {
                is SocketException, is InterruptedIOException ->
                    DialogUtil.showError("A connection error occurred during database sync. Check your internet connection and try again.")
                else -> DialogUtil.showError("An unexpected error occurred during database sync. No data has been changed.")
            }

            throw e
        }
    }

    private fun makeDatabaseMerger(remoteDatabase: Database, remoteName: String)
      = DatabaseMerger(mainDatabase, remoteDatabase, DatabaseMigrator(DatabaseMigrations.getConversionsMap()), remoteName)

    private fun getSyncConfig(): SyncConfig?
    {
        val remoteName = getRemoteName()
        return if (remoteName.isEmpty()) doFirstTimeSetup() else SyncConfig(SyncMode.NORMAL_SYNC, remoteName)
    }

    private fun doFirstTimeSetup(): SyncConfig?
    {
        val remoteName = promptForNonEmptyInput("Sync Setup", "Enter a unique name for the synced database (case-sensitive)")
        if (dbStore.databaseExists(remoteName))
        {
            val options = listOf("Overwrite local data", "Sync with local data", "Cancel")
            val response = DialogUtil.showOption("Database found",
                "Remote database '$remoteName' already exists. How would you like to proceed?", options)

            if (response == null || response == "Cancel")
            {
                return null
            }

            val choice = if (response == "Overwrite local data") SyncMode.OVERWRITE_LOCAL else SyncMode.NORMAL_SYNC
            return SyncConfig(choice, remoteName)
        }
        else
        {
            val options = listOf("Create '$remoteName'", "Cancel")
            val response = DialogUtil.showOption("Database not found",
                "No remote database found called '$remoteName'. Would you like to create it", options)

            if (response == null || response == "Cancel")
            {
                return null
            }

            return SyncConfig(SyncMode.CREATE_REMOTE, remoteName)
        }
    }

    private fun validateSync(): Boolean
    {
        val openScreens = ScreenCache.getDartsGameScreens()
        if (openScreens.isNotEmpty())
        {
            DialogUtil.showError("You must close all open games before starting a sync.")
            return false
        }

        return true
    }
}