package dartzee.db

import dartzee.core.util.DialogUtil
import dartzee.logging.CODE_MERGE_ERROR
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings.logger

class DatabaseMerger(private val localDatabase: Database,
                     private val remoteDatabase: Database,
                     private val migrator: DatabaseMigrator,
                     private val remoteName: String)
{
    fun validateMerge(): Boolean
    {
        if (!remoteDatabase.testConnection())
        {
            DialogUtil.showError("An error occurred connecting to the remote database.")
            return false
        }

        val remoteVersion = remoteDatabase.getDatabaseVersion()
        if (remoteVersion == null)
        {
            logger.error(CODE_MERGE_ERROR, "Unable to ascertain remote database version (but could connect). Wat?")
            DialogUtil.showError("An error occurred connecting to the remote database.")
            return false
        }

        if (remoteVersion > DartsDatabaseUtil.DATABASE_VERSION)
        {
            val error = "The remote database contains data written by a higher Dartzee version. \n\nYou will need to update to the latest version of Dartzee before syncing again."
            DialogUtil.showError(error)
            return false
        }

        return true
    }

    fun performMerge(): Database?
    {
        val result = migrator.migrateToLatest(remoteDatabase, "Remote")
        if (result != MigrationResult.SUCCESS)
        {
            return null
        }

        val lastLocalSync = SyncAuditEntity.getLastSyncDate(localDatabase, remoteName)
        DartsDatabaseUtil.getAllEntities(localDatabase).forEach { dao ->
            val entities = dao.retrieveModifiedSince(lastLocalSync)

        }

        SyncAuditEntity.insertSyncAudit(remoteDatabase, remoteName)

        return remoteDatabase
    }
}