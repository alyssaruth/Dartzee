package dartzee.sync

import dartzee.core.util.DialogUtil

class SyncConfigurer(private val dbStore: IRemoteDatabaseStore)
{
    fun doFirstTimeSetup(): SyncConfig?
    {
        val remoteName = DialogUtil.showInput<String>("Sync Setup", "Enter a unique name for the shared database (case-sensitive)")
        if (remoteName.isNullOrBlank())
        {
            return null
        }

        if (dbStore.databaseExists(remoteName))
        {
            val options = listOf("Overwrite local data", "Sync with local data", "Cancel")
            val response = DialogUtil.showOption("Database found",
                "Shared database '$remoteName' already exists. How would you like to proceed?", options)

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
                "No shared database found called '$remoteName'. Would you like to create it?", options)

            if (response == null || response == "Cancel")
            {
                return null
            }

            return SyncConfig(SyncMode.CREATE_REMOTE, remoteName)
        }
    }
}