package dartzee.sync

import dartzee.core.util.DialogUtil
import dartzee.core.util.formatTimestamp
import dartzee.db.GameEntity
import dartzee.db.SyncAuditEntity
import dartzee.screen.MenuScreen
import dartzee.screen.ScreenCache
import dartzee.screen.sync.SyncManagementScreen
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
    ScreenCache.get<SyncManagementScreen>().initialise()
}

fun validateSyncAction(): Boolean
{
    val openScreens = ScreenCache.getDartsGameScreens()
    if (openScreens.isNotEmpty())
    {
        DialogUtil.showError("You must close all open games before performing this action.")
        return false
    }

    return true
}