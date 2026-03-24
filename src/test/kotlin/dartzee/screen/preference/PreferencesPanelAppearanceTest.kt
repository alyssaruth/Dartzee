package dartzee.screen.preference

import com.github.alyssaburlton.swingtest.getChild
import dartzee.preferences.Preferences
import dartzee.theme.ThemeId
import dartzee.theme.ThemeSelector
import dartzee.utils.InjectedThings.preferenceService
import io.kotest.matchers.shouldBe

class PreferencesPanelAppearanceTest : AbstractPreferencePanelTest<PreferencesPanelAppearance>() {
    override fun factory() = PreferencesPanelAppearance()

    override fun checkUiFieldValuesAreDefaults(panel: PreferencesPanelAppearance) {
        panel.spinnerHueFactor.value shouldBe 0.8
        panel.spinnerFgBrightness.value shouldBe 0.5
        panel.spinnerBgBrightness.value shouldBe 1.0
        panel.getChild<ThemeSelector>().selectedThemeId() shouldBe ThemeId.None
    }

    override fun setUiFieldValuesToNonDefaults(panel: PreferencesPanelAppearance) {
        panel.spinnerHueFactor.value = 0.5
        panel.spinnerFgBrightness.value = 0.9
        panel.spinnerBgBrightness.value = 0.6
        panel.getChild<ThemeSelector>().selectTheme(ThemeId.Easter)
    }

    override fun checkUiFieldValuesAreNonDefaults(panel: PreferencesPanelAppearance) {
        panel.spinnerHueFactor.value shouldBe 0.5
        panel.spinnerFgBrightness.value shouldBe 0.9
        panel.spinnerBgBrightness.value shouldBe 0.6
        panel.getChild<ThemeSelector>().selectedThemeId() shouldBe ThemeId.Easter
    }

    override fun checkPreferencesAreSetToNonDefaults() {
        preferenceService.get(Preferences.hueFactor) shouldBe 0.5
        preferenceService.get(Preferences.fgBrightness) shouldBe 0.9
        preferenceService.get(Preferences.bgBrightness) shouldBe 0.6
        preferenceService.get(Preferences.theme) shouldBe ThemeId.Easter
    }
}
