package dartzee.screen.preference

import com.github.alyssaburlton.swingtest.uncheck
import dartzee.utils.PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE
import dartzee.utils.PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES
import dartzee.utils.PREFERENCES_BOOLEAN_SHOW_ANIMATIONS
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PREFERENCES_INT_LEADERBOARD_SIZE
import dartzee.utils.PreferenceUtil
import io.kotest.matchers.shouldBe

class TestPreferencesPanelMisc : AbstractPreferencePanelTest<PreferencesPanelMisc>() {
    override fun getPreferencesAffected(): MutableList<String> {
        return mutableListOf(
            PREFERENCES_INT_AI_SPEED,
            PREFERENCES_INT_LEADERBOARD_SIZE,
            PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE,
            PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES,
            PREFERENCES_BOOLEAN_SHOW_ANIMATIONS
        )
    }

    override fun factory() = PreferencesPanelMisc()

    override fun setUiFieldValuesToNonDefaults(panel: PreferencesPanelMisc) {
        panel.slider.value = 20
        panel.nfLeaderboardSize.value = 100

        panel.chckbxAiAutomaticallyFinish.uncheck()
        panel.chckbxCheckForUpdates.uncheck()
        panel.chckbxShowAnimations.uncheck()
    }

    override fun checkUiFieldValuesAreNonDefaults(panel: PreferencesPanelMisc) {
        panel.slider.value shouldBe 20
        panel.nfLeaderboardSize.value shouldBe 100
        panel.chckbxAiAutomaticallyFinish.isSelected shouldBe false
        panel.chckbxCheckForUpdates.isSelected shouldBe false
        panel.chckbxShowAnimations.isSelected shouldBe false
    }

    override fun checkPreferencesAreSetToNonDefaults() {
        PreferenceUtil.getIntValue(PREFERENCES_INT_AI_SPEED) shouldBe 20
        PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE) shouldBe 100
        PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE) shouldBe false
        PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES) shouldBe false
        PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS) shouldBe false
    }

    override fun checkUiFieldValuesAreDefaults(panel: PreferencesPanelMisc) {
        panel.slider.value shouldBe 1000
        panel.nfLeaderboardSize.value shouldBe 50
        panel.chckbxAiAutomaticallyFinish.isSelected shouldBe true
        panel.chckbxCheckForUpdates.isSelected shouldBe true
        panel.chckbxShowAnimations.isSelected shouldBe true
    }
}
