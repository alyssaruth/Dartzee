package dartzee.db

import dartzee.achievements.getAllAchievements
import dartzee.achievements.runConversionsWithProgressBar
import dartzee.core.util.DialogUtil
import dartzee.logging.CODE_MERGE_ERROR
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings.logger
import java.sql.Timestamp

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

        val result = migrator.migrateToLatest(remoteDatabase, "Remote")
        if (result != MigrationResult.SUCCESS)
        {
            return false
        }

        return true
    }

    fun performMerge(): Database
    {
        val lastLocalSync = SyncAuditEntity.getLastSyncDate(localDatabase, remoteName)
        getSyncEntities().forEach { dao -> syncRowsFromTable(dao, lastLocalSync) }

        val playersAffected = DartEntity().retrieveModifiedSince(lastLocalSync).map { it.playerId }.distinct()
        if (playersAffected.isNotEmpty())
        {
            val t = runConversionsWithProgressBar(getAllAchievements(), playersAffected, remoteDatabase)
            t.join()
        }

        SyncAuditEntity.insertSyncAudit(remoteDatabase, remoteName)

        return remoteDatabase
    }

    private fun syncRowsFromTable(localDao: AbstractEntity<*>, lastSync: Timestamp?)
    {
        val rows = localDao.retrieveModifiedSince(lastSync)
        rows.forEach { it.mergeIntoDatabase(remoteDatabase) }
    }

    private fun getSyncEntities(): List<AbstractEntity<*>>
    {
        val entities = DartsDatabaseUtil.getAllEntities(localDatabase)
        return entities.filter { it.includeInSync() }
    }
}