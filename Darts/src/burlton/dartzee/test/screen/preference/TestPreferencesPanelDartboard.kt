package burlton.dartzee.test.screen.preference

import burlton.dartzee.code.screen.preference.PreferencesPanelDartboard
import burlton.dartzee.code.utils.*
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
        panel.cpOddSingle.selectedColor shouldBe DartsColour.DARTBOARD_WHITE
        panel.cpOddDouble.selectedColor shouldBe DartsColour.DARTBOARD_GREEN
        panel.cpOddTreble.selectedColor shouldBe DartsColour.DARTBOARD_GREEN

        panel.cpEvenSingle.selectedColor shouldBe DartsColour.DARTBOARD_BLACK
        panel.cpEvenDouble.selectedColor shouldBe DartsColour.DARTBOARD_RED
        panel.cpEvenTreble.selectedColor shouldBe DartsColour.DARTBOARD_RED
    }

    override fun setUiFieldValuesToNonDefaults(panel: PreferencesPanelDartboard)
    {
        panel.cpOddSingle.selectedColor = Color.BLUE
        panel.cpOddDouble.selectedColor = Color(200, 50, 128)
        panel.cpOddTreble.selectedColor = Color.getHSBColor(0.9f, 0.8f, 1.0f)

        panel.cpEvenSingle.selectedColor = Color.YELLOW
        panel.cpEvenDouble.selectedColor = Color.MAGENTA
        panel.cpEvenTreble.selectedColor = Color.CYAN
    }

    override fun checkUiFieldValuesAreNonDefaults(panel: PreferencesPanelDartboard)
    {
        panel.cpOddSingle.selectedColor shouldBe Color.BLUE
        panel.cpOddDouble.selectedColor shouldBe Color(200, 50, 128)
        panel.cpOddTreble.selectedColor shouldBe Color.getHSBColor(0.9f, 0.8f, 1.0f)

        panel.cpEvenSingle.selectedColor shouldBe Color.YELLOW
        panel.cpEvenDouble.selectedColor shouldBe Color.MAGENTA
        panel.cpEvenTreble.selectedColor shouldBe Color.CYAN
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