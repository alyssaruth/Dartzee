package dartzee.db

import dartzee.achievements.getAchievementForType
import dartzee.achievements.runConversionsWithProgressBar
import dartzee.logging.CODE_MERGE_STARTED
import dartzee.logging.CODE_MERGING_ENTITY
import dartzee.logging.KEY_ROW_COUNT
import dartzee.logging.KEY_TABLE_NAME
import dartzee.screen.sync.SyncProgressDialog
import dartzee.sync.SyncStage
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings.logger
import java.sql.Timestamp

class DatabaseMerger(private val localDatabase: Database,
                     private val remoteDatabase: Database,
                     private val remoteName: String)
{
    fun performMerge(): Database
    {
        SyncProgressDialog.progressToStage(SyncStage.MERGE_LOCAL_CHANGES)

        val lastLocalSync = SyncAuditEntity.getLastSyncData(localDatabase)?.lastSynced
        logger.info(CODE_MERGE_STARTED, "Starting merge - last local sync $lastLocalSync")
        getSyncEntities().forEach { dao -> syncRowsFromTable(dao, lastLocalSync) }

        SyncProgressDialog.progressToStage(SyncStage.UPDATE_ACHIEVEMENTS)

        val achievementsChanged = AchievementEntity().retrieveModifiedSince(lastLocalSync)
        if (achievementsChanged.isNotEmpty())
        {
            val players = achievementsChanged.map { it.playerId }.distinct()
            val achievementTypes = achievementsChanged.map { it.achievementType }.distinct()
            val achievements = achievementTypes.mapNotNull(::getAchievementForType)
            val t = runConversionsWithProgressBar(achievements, players, remoteDatabase)
            t.join()
        }

        SyncAuditEntity.insertSyncAudit(remoteDatabase, remoteName)
        return remoteDatabase
    }

    private fun syncRowsFromTable(localDao: AbstractEntity<*>, lastSync: Timestamp?)
    {
        val rows = localDao.retrieveModifiedSince(lastSync)
        val tableName = localDao.getTableName()
        logger.info(CODE_MERGING_ENTITY, "Merging ${rows.size} rows from ${localDao.getTableName()}",
            KEY_TABLE_NAME to tableName, KEY_ROW_COUNT to rows.size)

        rows.forEach { it.mergeIntoDatabase(remoteDatabase) }
    }

    private fun getSyncEntities(): List<AbstractEntity<*>>
    {
        val entities = DartsDatabaseUtil.getAllEntities(localDatabase)
        return entities.filter { it.includeInSync() }
    }
}