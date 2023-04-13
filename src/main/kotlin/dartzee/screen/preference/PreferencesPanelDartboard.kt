package dartzee.screen.preference

import dartzee.bean.PresentationDartboard
import dartzee.core.bean.ColourPicker
import dartzee.core.bean.ColourSelectionListener
import dartzee.core.util.setFontSize
import dartzee.`object`.ColourWrapper
import dartzee.utils.DartsColour
import dartzee.utils.PREFERENCES_STRING_EVEN_DOUBLE_COLOUR
import dartzee.utils.PREFERENCES_STRING_EVEN_SINGLE_COLOUR
import dartzee.utils.PREFERENCES_STRING_EVEN_TREBLE_COLOUR
import dartzee.utils.PREFERENCES_STRING_ODD_DOUBLE_COLOUR
import dartzee.utils.PREFERENCES_STRING_ODD_SINGLE_COLOUR
import dartzee.utils.PREFERENCES_STRING_ODD_TREBLE_COLOUR
import dartzee.utils.PreferenceUtil
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel

class PreferencesPanelDartboard : AbstractPreferencesPanel(), ColourSelectionListener
{
    override val title = "Dartboard"

    private val panelCenter = JPanel()
    private val panelEast = JPanel()
    val cpOddSingle = ColourPicker()
    val cpOddDouble = ColourPicker()
    val cpOddTreble = ColourPicker()
    val cpEvenSingle = ColourPicker()
    val cpEvenDouble = ColourPicker()
    val cpEvenTreble = ColourPicker()
    private var dartboard = PresentationDartboard(renderScoreLabels = true)

    init
    {
        panelCenter.layout = BorderLayout()
        add(panelCenter, BorderLayout.CENTER)
        panelCenter.add(dartboard, BorderLayout.CENTER)
        panelCenter.add(panelEast, BorderLayout.EAST)

        panelEast.layout = MigLayout("al center center, gapy 20", "[][][]", "[][][]")
        val lblSingleColours = JLabel("Single Colours")
        lblSingleColours.setFontSize(16)
        panelEast.add(lblSingleColours, "cell 0 0")
        panelEast.add(cpOddSingle, "cell 1 0")
        panelEast.add(cpEvenSingle, "cell 2 0")

        val lblDoubleColours = JLabel("Double Colours")
        lblDoubleColours.setFontSize(16)
        panelEast.add(lblDoubleColours, "cell 0 1")
        panelEast.add(cpOddDouble, "cell 1 1")
        panelEast.add(cpEvenDouble, "cell 2 1")

        val lblTrebleColours = JLabel("Treble Colours")
        lblTrebleColours.setFontSize(16)
        panelEast.add(lblTrebleColours, "cell 0 2")
        panelEast.add(cpOddTreble, "cell 1 2")
        panelEast.add(cpEvenTreble, "cell 2 2")

        cpOddSingle.addColourSelectionListener(this)
        cpOddDouble.addColourSelectionListener(this)
        cpOddTreble.addColourSelectionListener(this)
        cpEvenSingle.addColourSelectionListener(this)
        cpEvenDouble.addColourSelectionListener(this)
        cpEvenTreble.addColourSelectionListener(this)
    }

    override fun refreshImpl(useDefaults: Boolean)
    {
        val evenSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_SINGLE_COLOUR, useDefaults)
        val evenDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR, useDefaults)
        val evenTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_TREBLE_COLOUR, useDefaults)
        val oddSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_SINGLE_COLOUR, useDefaults)
        val oddDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_DOUBLE_COLOUR, useDefaults)
        val oddTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_TREBLE_COLOUR, useDefaults)

        val evenSingle = DartsColour.getColorFromPrefStr(evenSingleStr)
        val evenDouble = DartsColour.getColorFromPrefStr(evenDoubleStr)
        val evenTreble = DartsColour.getColorFromPrefStr(evenTrebleStr)

        val oddSingle = DartsColour.getColorFromPrefStr(oddSingleStr)
        val oddDouble = DartsColour.getColorFromPrefStr(oddDoubleStr)
        val oddTreble = DartsColour.getColorFromPrefStr(oddTrebleStr)

        cpOddSingle.updateSelectedColor(oddSingle, notify = false)
        cpOddDouble.updateSelectedColor(oddDouble, notify = false)
        cpOddTreble.updateSelectedColor(oddTreble, notify = false)
        cpEvenSingle.updateSelectedColor(evenSingle, notify = false)
        cpEvenDouble.updateSelectedColor(evenDouble, notify = false)
        cpEvenTreble.updateSelectedColor(evenTreble, notify = false)

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

        panelCenter.remove(dartboard)
        dartboard = PresentationDartboard(wrapper, renderScoreLabels = true)
        panelCenter.add(dartboard, BorderLayout.CENTER)
        panelCenter.revalidate()
        panelCenter.repaint()
    }

    override fun saveImpl()
    {
        val oddSingleStr = cpOddSingle.getPrefString()
        val oddDoubleStr = cpOddDouble.getPrefString()
        val oddTrebleStr = cpOddTreble.getPrefString()
        val evenSingleStr = cpEvenSingle.getPrefString()
        val evenDoubleStr = cpEvenDouble.getPrefString()
        val evenTrebleStr = cpEvenTreble.getPrefString()

        PreferenceUtil.saveString(PREFERENCES_STRING_ODD_SINGLE_COLOUR, oddSingleStr)
        PreferenceUtil.saveString(PREFERENCES_STRING_ODD_DOUBLE_COLOUR, oddDoubleStr)
        PreferenceUtil.saveString(PREFERENCES_STRING_ODD_TREBLE_COLOUR, oddTrebleStr)
        PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_SINGLE_COLOUR, evenSingleStr)
        PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR, evenDoubleStr)
        PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_TREBLE_COLOUR, evenTrebleStr)
    }

    override fun hasOutstandingChanges() =
            !cpOddSingle.matchesPreference(PREFERENCES_STRING_ODD_SINGLE_COLOUR)
                    || !cpOddDouble.matchesPreference(PREFERENCES_STRING_ODD_DOUBLE_COLOUR)
                    || !cpOddTreble.matchesPreference(PREFERENCES_STRING_ODD_TREBLE_COLOUR)
                    || !cpEvenSingle.matchesPreference(PREFERENCES_STRING_EVEN_SINGLE_COLOUR)
                    || !cpEvenDouble.matchesPreference(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR)
                    || !cpEvenTreble.matchesPreference(PREFERENCES_STRING_EVEN_TREBLE_COLOUR)

    private fun ColourPicker.matchesPreference(preferenceKey: String) =
        getPrefString() == PreferenceUtil.getStringValue(preferenceKey)

    override fun colourSelected(colour: Color)
    {
        refreshDartboard()
        stateChanged()
    }
}
