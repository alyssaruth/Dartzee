package burlton.dartzee.test.helper

import burlton.dartzee.code.utils.PreferenceUtil
import org.junit.After

abstract class AbstractRegistryTest: AbstractTest()
{
    private val hmPreferenceToSetting = mutableMapOf<String, String>()

    abstract fun getPreferencesAffected(): MutableList<String>

    override fun beforeClass()
    {
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