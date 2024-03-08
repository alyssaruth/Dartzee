package dartzee.screen.preference

import com.github.alyssaburlton.swingtest.uncheck
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings.preferenceService
import io.kotest.matchers.shouldBe

class TestPreferencesPanelMisc : AbstractPreferencePanelTest<PreferencesPanelMisc>() {
    override fun getPreferencesAffected(): MutableList<String> {
        return mutableListOf()
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
        preferenceService.get(Preferences.aiSpeed) shouldBe 20
        preferenceService.get(Preferences.leaderboardSize) shouldBe 100
        preferenceService.get(Preferences.aiAutoContinue) shouldBe false
        preferenceService.get(Preferences.checkForUpdates) shouldBe false
        preferenceService.get(Preferences.showAnimations) shouldBe false
    }

    override fun checkUiFieldValuesAreDefaults(panel: PreferencesPanelMisc) {
        panel.slider.value shouldBe 1000
        panel.nfLeaderboardSize.value shouldBe 50
        panel.chckbxAiAutomaticallyFinish.isSelected shouldBe true
        panel.chckbxCheckForUpdates.isSelected shouldBe true
        panel.chckbxShowAnimations.isSelected shouldBe true
    }
}
