package dartzee.sync

import dartzee.core.util.DialogUtil
import dartzee.main.promptForNonEmptyInput
import dartzee.screen.ScreenCache

class SyncConfigurer(private val dbStore: IRemoteDatabaseStore)
{
    fun validateAndConfigureSync(): SyncConfig?
    {
        if (!validateSync())
        {
            return null
        }

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
                "No remote database found called '$remoteName'. Would you like to create it?", options)

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