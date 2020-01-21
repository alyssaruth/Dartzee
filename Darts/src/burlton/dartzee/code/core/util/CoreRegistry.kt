package burlton.dartzee.code.core.util

import java.util.prefs.Preferences

object CoreRegistry
{
    val instance: Preferences = Preferences.userRoot().node("entropyInstance")
    const val INSTANCE_STRING_USER_NAME = "userName"
}
