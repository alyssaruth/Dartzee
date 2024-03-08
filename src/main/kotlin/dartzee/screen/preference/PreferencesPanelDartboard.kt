package dartzee.screen.preference

import dartzee.bean.InteractiveDartboard
import dartzee.core.bean.ColourPicker
import dartzee.core.bean.ColourSelectionListener
import dartzee.core.util.setFontSize
import dartzee.`object`.ColourWrapper
import dartzee.preferences.Preference
import dartzee.preferences.Preferences
import dartzee.utils.DartsColour
import dartzee.utils.InjectedThings.preferenceService
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JPanel
import net.miginfocom.swing.MigLayout

class PreferencesPanelDartboard : AbstractPreferencesPanel(), ColourSelectionListener {
    override val title = "Dartboard"

    private val panelCenter = JPanel()
    private val panelEast = JPanel()
    val cpOddSingle = ColourPicker()
    val cpOddDouble = ColourPicker()
    val cpOddTreble = ColourPicker()
    val cpEvenSingle = ColourPicker()
    val cpEvenDouble = ColourPicker()
    val cpEvenTreble = ColourPicker()
    private var dartboard = InteractiveDartboard()

    init {
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
        panelEast.preferredSize = Dimension(300, 100)

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

    override fun refreshImpl(useDefaults: Boolean) {
        val evenSingleStr = preferenceService.get(Preferences.evenSingleColour, useDefaults)
        val evenDoubleStr = preferenceService.get(Preferences.evenDoubleColour, useDefaults)
        val evenTrebleStr = preferenceService.get(Preferences.evenTrebleColour, useDefaults)
        val oddSingleStr = preferenceService.get(Preferences.oddSingleColour, useDefaults)
        val oddDoubleStr = preferenceService.get(Preferences.oddDoubleColour, useDefaults)
        val oddTrebleStr = preferenceService.get(Preferences.oddTrebleColour, useDefaults)

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

    private fun refreshDartboard() {
        val oddSingle = cpOddSingle.selectedColour
        val oddDouble = cpOddDouble.selectedColour
        val oddTreble = cpOddTreble.selectedColour
        val evenSingle = cpEvenSingle.selectedColour
        val evenDouble = cpEvenDouble.selectedColour
        val evenTreble = cpEvenTreble.selectedColour

        val wrapper =
            ColourWrapper(
                evenSingle,
                evenDouble,
                evenTreble,
                oddSingle,
                oddDouble,
                oddTreble,
                evenDouble,
                oddDouble
            )

        panelCenter.remove(dartboard)
        dartboard = InteractiveDartboard(wrapper)
        panelCenter.add(dartboard, BorderLayout.CENTER)
        panelCenter.revalidate()
        panelCenter.repaint()
    }

    override fun saveImpl() {
        preferenceService.save(Preferences.oddSingleColour, cpOddSingle.getPrefString())
        preferenceService.save(Preferences.oddDoubleColour, cpOddDouble.getPrefString())
        preferenceService.save(Preferences.oddTrebleColour, cpOddTreble.getPrefString())
        preferenceService.save(Preferences.evenSingleColour, cpEvenSingle.getPrefString())
        preferenceService.save(Preferences.evenDoubleColour, cpEvenDouble.getPrefString())
        preferenceService.save(Preferences.evenTrebleColour, cpEvenTreble.getPrefString())
    }

    override fun hasOutstandingChanges() =
        !cpOddSingle.matchesPreference(Preferences.oddSingleColour) ||
            !cpOddDouble.matchesPreference(Preferences.oddDoubleColour) ||
            !cpOddTreble.matchesPreference(Preferences.oddTrebleColour) ||
            !cpEvenSingle.matchesPreference(Preferences.evenSingleColour) ||
            !cpEvenDouble.matchesPreference(Preferences.evenDoubleColour) ||
            !cpEvenTreble.matchesPreference(Preferences.evenTrebleColour)

    private fun ColourPicker.matchesPreference(pref: Preference<String>) =
        getPrefString() == preferenceService.get(pref)

    override fun colourSelected(colour: Color) {
        refreshDartboard()
        stateChanged()
    }
}
