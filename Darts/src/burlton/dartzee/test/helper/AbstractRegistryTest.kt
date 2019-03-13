package burlton.dartzee.test.helper

import burlton.dartzee.code.utils.PreferenceUtil
import org.junit.After
import org.junit.Before

abstract class AbstractRegistryTest
{
    private val hmPreferenceToSetting = mutableMapOf<String, String>()

    abstract fun getPreferencesAffected(): MutableList<String>

    @Before
    fun cachePreferenceValues()
    {
        if (!hmPreferenceToSetting.isEmpty())
        {
            //We've already done the caching - no need to bother again
            return
        }

        getPreferencesAffected().forEach {
            hmPreferenceToSetting[it] = PreferenceUtil.getStringValue(it)
        }
    }

    fun clearPreferences()
    {
        getPreferencesAffected().forEach {
            PreferenceUtil.deleteSetting(it)
        }
    }

    @After
    fun restorePreferenceValues()
    {
        getPreferencesAffected().forEach {
            PreferenceUtil.saveString(it, hmPreferenceToSetting[it])
        }
    }
}