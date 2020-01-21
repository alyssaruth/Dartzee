package dartzee.test.screen.preference

import dartzee.screen.preference.PreferencesPanelDartboard
import dartzee.utils.*
import io.kotlintest.shouldBe
import java.awt.Color

class TestPreferencesPanelDartboard: AbstractPreferencePanelTest<PreferencesPanelDartboard>()
{
    override fun factory() = PreferencesPanelDartboard()

    override fun getPreferencesAffected(): MutableList<String>
    {
        return mutableListOf(PREFERENCES_STRING_EVEN_SINGLE_COLOUR,
                PREFERENCES_STRING_EVEN_DOUBLE_COLOUR,
                PREFERENCES_STRING_EVEN_TREBLE_COLOUR,
                PREFERENCES_STRING_ODD_SINGLE_COLOUR,
                PREFERENCES_STRING_ODD_DOUBLE_COLOUR,
                PREFERENCES_STRING_ODD_TREBLE_COLOUR)
    }

    override fun checkUiFieldValuesAreDefaults(panel: PreferencesPanelDartboard)
    {
        panel.cpOddSingle.selectedColour shouldBe DartsColour.DARTBOARD_WHITE
        panel.cpOddDouble.selectedColour shouldBe DartsColour.DARTBOARD_GREEN
        panel.cpOddTreble.selectedColour shouldBe DartsColour.DARTBOARD_GREEN

        panel.cpEvenSingle.selectedColour shouldBe DartsColour.DARTBOARD_BLACK
        panel.cpEvenDouble.selectedColour shouldBe DartsColour.DARTBOARD_RED
        panel.cpEvenTreble.selectedColour shouldBe DartsColour.DARTBOARD_RED
    }

    override fun setUiFieldValuesToNonDefaults(panel: PreferencesPanelDartboard)
    {
        panel.cpOddSingle.selectedColour = Color.BLUE
        panel.cpOddDouble.selectedColour = Color(200, 50, 128)
        panel.cpOddTreble.selectedColour = Color.getHSBColor(0.9f, 0.8f, 1.0f)

        panel.cpEvenSingle.selectedColour = Color.YELLOW
        panel.cpEvenDouble.selectedColour = Color.MAGENTA
        panel.cpEvenTreble.selectedColour = Color.CYAN
    }

    override fun checkUiFieldValuesAreNonDefaults(panel: PreferencesPanelDartboard)
    {
        panel.cpOddSingle.selectedColour shouldBe Color.BLUE
        panel.cpOddDouble.selectedColour shouldBe Color(200, 50, 128)
        panel.cpOddTreble.selectedColour shouldBe Color.getHSBColor(0.9f, 0.8f, 1.0f)

        panel.cpEvenSingle.selectedColour shouldBe Color.YELLOW
        panel.cpEvenDouble.selectedColour shouldBe Color.MAGENTA
        panel.cpEvenTreble.selectedColour shouldBe Color.CYAN
    }

    override fun checkPreferencesAreSetToNonDefaults()
    {
        val evenSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_SINGLE_COLOUR)
        val evenDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR)
        val evenTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_TREBLE_COLOUR)
        val oddSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_SINGLE_COLOUR)
        val oddDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_DOUBLE_COLOUR)
        val oddTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_TREBLE_COLOUR)

        DartsColour.getColorFromPrefStr(oddSingleStr, null) shouldBe Color.BLUE
        DartsColour.getColorFromPrefStr(oddDoubleStr, null) shouldBe Color(200, 50, 128)
        DartsColour.getColorFromPrefStr(oddTrebleStr, null) shouldBe Color.getHSBColor(0.9f, 0.8f, 1.0f)

        DartsColour.getColorFromPrefStr(evenSingleStr, null) shouldBe Color.YELLOW
        DartsColour.getColorFromPrefStr(evenDoubleStr, null) shouldBe Color.MAGENTA
        DartsColour.getColorFromPrefStr(evenTrebleStr, null) shouldBe Color.CYAN


    }
}