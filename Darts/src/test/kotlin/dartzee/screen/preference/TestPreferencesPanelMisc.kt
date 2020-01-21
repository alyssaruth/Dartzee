package dartzee.test.screen.preference

import dartzee.screen.preference.PreferencesPanelMisc
import dartzee.utils.*
import io.kotlintest.shouldBe

class TestPreferencesPanelMisc: AbstractPreferencePanelTest<PreferencesPanelMisc>()
{
    override fun getPreferencesAffected(): MutableList<String>
    {
        return mutableListOf(PREFERENCES_INT_AI_SPEED,
                PREFERENCES_INT_LEADERBOARD_SIZE,
                PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE,
                PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES,
                PREFERENCES_BOOLEAN_SHOW_ANIMATIONS,
                PREFERENCES_BOOLEAN_PRE_LOAD_RESOURCES)
    }

    override fun factory(): PreferencesPanelMisc
    {
        return PreferencesPanelMisc()
    }

    override fun setUiFieldValuesToNonDefaults(panel: PreferencesPanelMisc)
    {
        panel.slider.value = 20
        panel.nfLeaderboardSize.value = 100
        panel.chckbxAiAutomaticallyFinish.isSelected = false
        panel.chckbxCheckForUpdates.isSelected = false
        panel.chckbxShowAnimations.isSelected = false
        panel.chckbxPreloadResources.isSelected = false
    }

    override fun checkUiFieldValuesAreNonDefaults(panel: PreferencesPanelMisc)
    {
        panel.slider.value shouldBe 20
        panel.nfLeaderboardSize.value shouldBe 100
        panel.chckbxAiAutomaticallyFinish.isSelected shouldBe false
        panel.chckbxCheckForUpdates.isSelected shouldBe false
        panel.chckbxShowAnimations.isSelected shouldBe false
        panel.chckbxPreloadResources.isSelected shouldBe false
    }

    override fun checkPreferencesAreSetToNonDefaults()
    {
        PreferenceUtil.getIntValue(PREFERENCES_INT_AI_SPEED) shouldBe 20
        PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE) shouldBe 100
        PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE) shouldBe false
        PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES) shouldBe false
        PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS) shouldBe false
        PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_PRE_LOAD_RESOURCES) shouldBe false
    }

    override fun checkUiFieldValuesAreDefaults(panel: PreferencesPanelMisc)
    {
        panel.slider.value shouldBe 1000
        panel.nfLeaderboardSize.value shouldBe 50
        panel.chckbxAiAutomaticallyFinish.isSelected shouldBe true
        panel.chckbxPreloadResources.isSelected shouldBe true
        panel.chckbxCheckForUpdates.isSelected shouldBe true
        panel.chckbxShowAnimations.isSelected shouldBe true
    }
}