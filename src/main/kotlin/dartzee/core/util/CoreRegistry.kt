package dartzee.core.util

import java.util.prefs.Preferences

object CoreRegistry
{
    val instance: Preferences = Preferences.userRoot().node("entropyInstance")
    const val INSTANCE_STRING_USER_NAME = "userName"
    const val INSTANCE_STRING_DEVICE_ID = "deviceId"
}
