package dartzee.test.helper

import dartzee.utils.PreferenceUtil

abstract class AbstractRegistryTest: AbstractTest()
{
    private val hmPreferenceToSetting = mutableMapOf<String, String>()

    abstract fun getPreferencesAffected(): List<String>

    override fun doClassSetup()
    {
        super.doClassSetup()

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

    override fun afterEachTest()
    {
        super.afterEachTest()

        getPreferencesAffected().forEach {
            PreferenceUtil.saveString(it, hmPreferenceToSetting[it])
        }
    }
}