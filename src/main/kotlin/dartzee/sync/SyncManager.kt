package dartzee.sync

import dartzee.core.util.DialogUtil
import dartzee.main.promptForNonEmptyInput
import dartzee.screen.ScreenCache

class SyncManager(private val dbStore: IRemoteDatabaseStore)
{
    fun doSync()
    {
        if (!validateSync())
        {
            return
        }

        val remoteName = getRemoteName()
        if (remoteName.isEmpty())
        {
            doFirstTimeSetup()
        }
    }

    private fun doFirstTimeSetup()
    {
        val remoteName = promptForNonEmptyInput("Sync Setup", "Enter a unique name for the synced database (case-sensitive)")
        if (dbStore.databaseExists(remoteName))
        {
            val response = DialogUtil.showQuestion("Remote database \"$remoteName\" already exists.")
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