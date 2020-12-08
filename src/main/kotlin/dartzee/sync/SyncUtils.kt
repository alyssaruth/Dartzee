package dartzee.sync

import dartzee.core.util.formatTimestamp
import dartzee.db.GameEntity
import dartzee.db.SyncAuditEntity
import dartzee.screen.MenuScreen
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.mainDatabase
import java.sql.Timestamp

const val SYNC_BUCKET_NAME = "dartzee-databases"

fun getRemoteName() = SyncAuditEntity.getLastSyncData(mainDatabase)?.remoteName ?: ""

enum class SyncMode
{
    CREATE_REMOTE,
    OVERWRITE_LOCAL,
    NORMAL_SYNC
}

data class SyncConfig(val mode: SyncMode, val remoteName: String)

data class LastSyncData(val remoteName: String, val lastSynced: Timestamp)
data class SyncSummary(val remoteName: String, val lastSynced: String, val pendingGames: String)
fun refreshSyncSummary()
{
    val version = mainDatabase.getDatabaseVersion() ?: return
    if (version >= 16)
    {
        val syncSummary = retrieveSyncSummary()
        ScreenCache.get<MenuScreen>().refreshSummary(syncSummary)
    }
}
private fun retrieveSyncSummary(): SyncSummary
{
    val lastSyncData = SyncAuditEntity.getLastSyncData(mainDatabase) ?: return SyncSummary("Unset", "-", "-")

    val lastSyncDesc = lastSyncData.lastSynced.formatTimestamp()
    val pendingGameCount = getModifiedGameCount()

    return SyncSummary(lastSyncData.remoteName, lastSyncDesc, "$pendingGameCount")
}

data class SyncResult(val gamesPushed: Int, val gamesPulled: Int)

fun getModifiedGameCount(): Int
{
    val lastSynced = SyncAuditEntity.getLastSyncData(mainDatabase)?.lastSynced
    return GameEntity().countModifiedSince(lastSynced)
}

fun resetRemote()
{
    SyncAuditEntity().deleteAll()
    refreshSyncSummary()
}
