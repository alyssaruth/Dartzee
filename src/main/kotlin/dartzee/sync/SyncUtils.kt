package dartzee.sync

import dartzee.core.util.formatTimestamp
import dartzee.db.GameEntity
import dartzee.db.SyncAuditEntity
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import dartzee.utils.PREFERENCES_STRING_REMOTE_DATABASE_NAME
import dartzee.utils.PreferenceUtil

const val SYNC_BUCKET_NAME = "dartzee-databases"

fun getRemoteName() = PreferenceUtil.getStringValue(PREFERENCES_STRING_REMOTE_DATABASE_NAME)
fun saveRemoteName(name: String)
{
    PreferenceUtil.saveString(PREFERENCES_STRING_REMOTE_DATABASE_NAME, name)
}

enum class SyncMode
{
    CREATE_REMOTE,
    OVERWRITE_LOCAL,
    NORMAL_SYNC
}

data class SyncConfig(val mode: SyncMode, val remoteName: String)

data class SyncSummary(val remoteName: String, val lastSynced: String, val pendingGames: String)
fun refreshSyncSummary()
{
    val remoteName = getRemoteName()
    val syncSummary = if (remoteName.isEmpty())
    {
        SyncSummary("Unset", "-", "-")
    }
    else
    {
        val dtLastSynced = SyncAuditEntity.getLastSyncDate(InjectedThings.mainDatabase, remoteName)
        val lastSyncDesc = dtLastSynced?.formatTimestamp() ?: "-"
        val pendingGameCount = GameEntity().countModifiedSince(dtLastSynced)

        SyncSummary(remoteName, lastSyncDesc, "$pendingGameCount")
    }

    ScreenCache.syncSummaryPanel.refreshSummary(syncSummary)
}

fun resetRemote()
{
    saveRemoteName("")
    SyncAuditEntity().deleteAll()
    refreshSyncSummary()
}
