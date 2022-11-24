package dartzee.helper

import dartzee.utils.PreferenceUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class AbstractRegistryTest: AbstractTest()
{
    private val hmPreferenceToSetting = mutableMapOf<String, String>()

    abstract fun getPreferencesAffected(): List<String>

    @BeforeEach
    fun cachePreferences()
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

    @AfterEach
    fun resetPreferences()
    {
        getPreferencesAffected().forEach {
            PreferenceUtil.saveString(it, hmPreferenceToSetting[it])
        }
    }
}