package dartzee.sync

import dartzee.utils.PREFERENCES_STRING_REMOTE_DATABASE_NAME
import dartzee.utils.PreferenceUtil

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