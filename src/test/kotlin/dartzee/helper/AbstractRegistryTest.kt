package dartzee.helper

import dartzee.utils.PreferenceUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll

abstract class AbstractRegistryTest: AbstractTest()
{
    private val hmPreferenceToSetting = mutableMapOf<String, String>()

    abstract fun getPreferencesAffected(): List<String>

    @BeforeAll
    fun beforeAll()
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
    fun afterEach()
    {
        getPreferencesAffected().forEach {
            PreferenceUtil.saveString(it, hmPreferenceToSetting[it])
        }
    }
}