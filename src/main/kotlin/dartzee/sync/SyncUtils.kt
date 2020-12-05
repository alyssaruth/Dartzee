package dartzee.sync

import dartzee.core.util.formatTimestamp
import dartzee.db.GameEntity
import dartzee.db.SyncAuditEntity
import dartzee.screen.MenuScreen
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.mainDatabase
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
    val version = mainDatabase.getDatabaseVersion() ?: return
    if (version >= 16)
    {
        val remoteName = getRemoteName()
        val syncSummary = if (remoteName.isEmpty())
        {
            SyncSummary("Unset", "-", "-")
        }
        else
        {
            val dtLastSynced = SyncAuditEntity.getLastSyncDate(mainDatabase, remoteName)
            val lastSyncDesc = dtLastSynced?.formatTimestamp() ?: "-"
            val pendingGameCount = getModifiedGameCount(remoteName)

            SyncSummary(remoteName, lastSyncDesc, "$pendingGameCount")
        }

        ScreenCache.get<MenuScreen>().refreshSummary(syncSummary)
    }
}

data class SyncResult(val gamesPushed: Int, val gamesPulled: Int)

fun getModifiedGameCount(remoteName: String): Int
{
    val dtLastSynced = SyncAuditEntity.getLastSyncDate(mainDatabase, remoteName)
    return GameEntity().countModifiedSince(dtLastSynced)
}

fun resetRemote()
{
    saveRemoteName("")
    SyncAuditEntity().deleteAll()
    refreshSyncSummary()
}
