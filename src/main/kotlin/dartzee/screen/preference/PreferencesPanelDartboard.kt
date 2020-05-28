package dartzee.screen.preference

import dartzee.`object`.ColourWrapper
import dartzee.core.bean.ColourPicker
import dartzee.core.bean.ColourSelectionListener
import dartzee.screen.Dartboard
import dartzee.utils.*
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.LineBorder

class PreferencesPanelDartboard : AbstractPreferencesPanel(), ColourSelectionListener
{
    private val panelCenter = JPanel()
    private val panelEast = JPanel()
    val cpOddSingle = ColourPicker()
    val cpOddDouble = ColourPicker()
    val cpOddTreble = ColourPicker()
    val cpEvenSingle = ColourPicker()
    val cpEvenDouble = ColourPicker()
    val cpEvenTreble = ColourPicker()
    private val dartboardPreview = Dartboard(400, 400)

    init
    {
        dartboardPreview.border = LineBorder(Color.BLACK)

        panelCenter.layout = FlowLayout(FlowLayout.CENTER)
        panelCenter.preferredSize = Dimension(400, 400)
        panelCenter.border = LineBorder(Color.RED)
        panelEast.border = LineBorder(Color.BLUE)
        add(panelCenter, BorderLayout.CENTER)
        add(panelEast, BorderLayout.EAST)
        panelCenter.add(dartboardPreview, BorderLayout.CENTER)

        panelEast.layout = MigLayout("al center center, wrap, gapy 20")
        val panelSingles = JPanel()
        val lblSingleColours = JLabel("Single Colours")
        panelSingles.add(lblSingleColours)
        panelSingles.add(cpOddSingle)
        panelSingles.add(cpEvenSingle)
        panelEast.add(panelSingles)

        val panelDoubles = JPanel()
        val lblDoubleColours = JLabel("Double Colours")
        panelDoubles.add(lblDoubleColours)
        panelDoubles.add(cpOddDouble)
        panelDoubles.add(cpEvenDouble)
        panelEast.add(panelDoubles)

        val panelTrebles = JPanel()
        val lblTrebleColours = JLabel("Treble Colours")
        panelTrebles.add(lblTrebleColours)
        panelTrebles.add(cpOddTreble)
        panelTrebles.add(cpEvenTreble)
        panelEast.add(panelTrebles)

        dartboardPreview.renderScoreLabels = true

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
