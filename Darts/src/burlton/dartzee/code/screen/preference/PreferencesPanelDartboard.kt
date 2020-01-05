package burlton.dartzee.code.screen.preference

import burlton.dartzee.code.`object`.ColourWrapper
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.utils.*
import burlton.desktopcore.code.bean.ColourPicker
import burlton.desktopcore.code.bean.ColourSelectionListener
import java.awt.Color
import java.awt.Font
import javax.swing.JLabel
import javax.swing.SwingConstants

class PreferencesPanelDartboard : AbstractPreferencesPanel(), ColourSelectionListener
{
    val cpOddSingle = ColourPicker()
    val cpOddDouble = ColourPicker()
    val cpOddTreble = ColourPicker()
    val cpEvenSingle = ColourPicker()
    val cpEvenDouble = ColourPicker()
    val cpEvenTreble = ColourPicker()
    private val dartboardPreview = Dartboard(200, 200)

    init
    {
        layout = null
        cpOddSingle.setBounds(110, 90, 30, 20)
        add(cpOddSingle)
        cpOddDouble.setBounds(110, 120, 30, 20)
        add(cpOddDouble)
        cpOddTreble.setBounds(110, 150, 30, 20)
        add(cpOddTreble)
        cpEvenSingle.setBounds(150, 90, 30, 20)
        add(cpEvenSingle)
        cpEvenDouble.setBounds(150, 120, 30, 20)
        add(cpEvenDouble)
        cpEvenTreble.setBounds(150, 150, 30, 20)
        add(cpEvenTreble)
        dartboardPreview.setBounds(250, 50, 200, 200)
        dartboardPreview.renderScoreLabels = true
        add(dartboardPreview)
        val lblSingleColours = JLabel("Single Colours")
        lblSingleColours.setBounds(15, 90, 91, 20)
        add(lblSingleColours)
        val lblDoubleColours = JLabel("Double Colours")
        lblDoubleColours.setBounds(15, 120, 91, 20)
        add(lblDoubleColours)
        val lblTrebleColours = JLabel("Treble Colours")
        lblTrebleColours.setBounds(15, 150, 91, 20)
        add(lblTrebleColours)
        val lblPreview = JLabel("Preview:")
        lblPreview.font = Font("Tahoma", Font.PLAIN, 14)
        lblPreview.horizontalAlignment = SwingConstants.CENTER
        lblPreview.setBounds(250, 20, 200, 24)
        add(lblPreview)

        cpOddSingle.addColourSelectionListener(this)
        cpOddDouble.addColourSelectionListener(this)
        cpOddTreble.addColourSelectionListener(this)
        cpEvenSingle.addColourSelectionListener(this)
        cpEvenDouble.addColourSelectionListener(this)
        cpEvenTreble.addColourSelectionListener(this)
    }

    override fun refresh(useDefaults: Boolean)
    {
        val evenSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_SINGLE_COLOUR, useDefaults)
        val evenDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR, useDefaults)
        val evenTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_TREBLE_COLOUR, useDefaults)
        val oddSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_SINGLE_COLOUR, useDefaults)
        val oddDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_DOUBLE_COLOUR, useDefaults)
        val oddTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_TREBLE_COLOUR, useDefaults)

        val evenSingle = DartsColour.getColorFromPrefStr(evenSingleStr, DartsColour.DARTBOARD_BLACK)
        val evenDouble = DartsColour.getColorFromPrefStr(evenDoubleStr, DartsColour.DARTBOARD_RED)
        val evenTreble = DartsColour.getColorFromPrefStr(evenTrebleStr, DartsColour.DARTBOARD_RED)

        val oddSingle = DartsColour.getColorFromPrefStr(oddSingleStr, DartsColour.DARTBOARD_WHITE)
        val oddDouble = DartsColour.getColorFromPrefStr(oddDoubleStr, DartsColour.DARTBOARD_GREEN)
        val oddTreble = DartsColour.getColorFromPrefStr(oddTrebleStr, DartsColour.DARTBOARD_GREEN)

        cpOddSingle.updateSelectedColor(oddSingle)
        cpOddDouble.updateSelectedColor(oddDouble)
        cpOddTreble.updateSelectedColor(oddTreble)
        cpEvenSingle.updateSelectedColor(evenSingle)
        cpEvenDouble.updateSelectedColor(evenDouble)
        cpEvenTreble.updateSelectedColor(evenTreble)

        refreshDartboard()
    }

    private fun refreshDartboard()
    {
        val oddSingle = cpOddSingle.selectedColour
        val oddDouble = cpOddDouble.selectedColour
        val oddTreble = cpOddTreble.selectedColour
        val evenSingle = cpEvenSingle.selectedColour
        val evenDouble = cpEvenDouble.selectedColour
        val evenTreble = cpEvenTreble.selectedColour

        val wrapper = ColourWrapper(evenSingle, evenDouble, evenTreble,
                oddSingle, oddDouble, oddTreble, evenDouble, oddDouble)

        dartboardPreview.paintDartboard(wrapper, true)
    }

    override fun valid() = true

    override fun save()
    {
        val oddSingle = cpOddSingle.selectedColour
        val oddDouble = cpOddDouble.selectedColour
        val oddTreble = cpOddTreble.selectedColour
        val evenSingle = cpEvenSingle.selectedColour
        val evenDouble = cpEvenDouble.selectedColour
        val evenTreble = cpEvenTreble.selectedColour

        val oddSingleStr = DartsColour.toPrefStr(oddSingle)
        val oddDoubleStr = DartsColour.toPrefStr(oddDouble)
        val oddTrebleStr = DartsColour.toPrefStr(oddTreble)
        val evenSingleStr = DartsColour.toPrefStr(evenSingle)
        val evenDoubleStr = DartsColour.toPrefStr(evenDouble)
        val evenTrebleStr = DartsColour.toPrefStr(evenTreble)

        PreferenceUtil.saveString(PREFERENCES_STRING_ODD_SINGLE_COLOUR, oddSingleStr)
        PreferenceUtil.saveString(PREFERENCES_STRING_ODD_DOUBLE_COLOUR, oddDoubleStr)
        PreferenceUtil.saveString(PREFERENCES_STRING_ODD_TREBLE_COLOUR, oddTrebleStr)
        PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_SINGLE_COLOUR, evenSingleStr)
        PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR, evenDoubleStr)
        PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_TREBLE_COLOUR, evenTrebleStr)
    }

    override fun colourSelected(colour: Color) = refreshDartboard()
}
