package dartzee.utils

import dartzee.core.util.Debug
import java.util.prefs.Preferences

object PreferenceUtil
{
    private const val PREFERENCE_DELIM_CHAR = ";"
    private val preferences = Preferences.userRoot().node(NODE_PREFERENCES)

    /**
     * Helpers
     */
    private fun getPrefAndDefault(prefStr: String): Pair<String, String>
    {
        val prefAndDefault = prefStr.split(PREFERENCE_DELIM_CHAR)
        if (prefAndDefault.size != 2) {
            Debug.stackTrace("Unexpected preference format: $prefStr")
        }
        return Pair(prefAndDefault[0], prefAndDefault[1])
    }

    private fun getRawPref(prefStr: String) = getPrefAndDefault(prefStr).first

    fun deleteSetting(prefStr: String)
    {
        val pref = getRawPref(prefStr)
        preferences.remove(pref)
    }

    /**
     * Strings
     */
    fun getStringValue(prefStr: String, returnDefault: Boolean = false): String
    {
        val (pref, default) = getPrefAndDefault(prefStr)
        return if (returnDefault) default else preferences[pref, default]
    }

    fun saveString(prefStr: String, value: String?)
    {
        val pref = getRawPref(prefStr)
        preferences.put(pref, value)
    }

    /**
     * Ints
     */
    fun getIntValue(prefStr: String, returnDefault: Boolean = false) = getStringValue(prefStr, returnDefault).toInt()
    fun saveInt(prefStr: String, value: Int)
    {
        val pref = getRawPref(prefStr)
        preferences.putInt(pref, value)
    }

    /**
     * Doubles!
     */
    fun getDoubleValue(prefStr: String, returnDefault: Boolean = false) = getStringValue(prefStr, returnDefault).toDouble()
    fun saveDouble(prefStr: String, value: Double)
    {
        val pref = getRawPref(prefStr)
        preferences.putDouble(pref, value)
    }

    /**
     * Bools
     */
    fun getBooleanValue(prefStr: String, returnDefault: Boolean = false) = getStringValue(prefStr, returnDefault).toBoolean()
    fun saveBoolean(prefStr: String, value: Boolean) {
        val pref = getRawPref(prefStr)
        preferences.putBoolean(pref, value)
    }
}