package dartzee.e2e

import dartzee.helper.AbstractRegistryTest
import dartzee.utils.PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE
import dartzee.utils.PREFERENCES_BOOLEAN_SHOW_ANIMATIONS
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag

@Tag("e2e")
abstract class AbstractE2ETest : AbstractRegistryTest() {
    override fun getPreferencesAffected() =
        listOf(
            PREFERENCES_INT_AI_SPEED,
            PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE,
            PREFERENCES_BOOLEAN_SHOW_ANIMATIONS
        )

    @BeforeEach
    open fun beforeEach() {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 0)
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, true)
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS, false)
    }
}
