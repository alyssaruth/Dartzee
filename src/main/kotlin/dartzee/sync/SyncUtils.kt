package dartzee.sync

import dartzee.utils.PREFERENCES_STRING_REMOTE_DATABASE_NAME
import dartzee.utils.PreferenceUtil

fun getRemoteName() = PreferenceUtil.getStringValue(PREFERENCES_STRING_REMOTE_DATABASE_NAME)