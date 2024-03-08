package dartzee.screen.preference

import dartzee.bean.SliderAiSpeed
import dartzee.core.bean.NumberField
import dartzee.core.util.setFontSize
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings.preferenceService
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import net.miginfocom.swing.MigLayout

class PreferencesPanelMisc :
    AbstractPreferencesPanel(), ActionListener, PropertyChangeListener, ChangeListener {
    override val title = "Misc"

    private val panelCenter = JPanel()
    private val lblDefaultAiSpeed = JLabel("Default AI speed")
    val slider = SliderAiSpeed()
    val chckbxAiAutomaticallyFinish = JCheckBox("AI automatically finish")
    val chckbxCheckForUpdates = JCheckBox("Automatically check for updates")
    private val lblRowsToShow = JLabel("Rows to show on Leaderboards")
    val nfLeaderboardSize = NumberField(10, 200)
    val chckbxShowAnimations = JCheckBox("Play sounds/animations")

    init {
        add(panelCenter, BorderLayout.CENTER)
        nfLeaderboardSize.columns = 10
        panelCenter.layout = MigLayout("", "[][grow][]", "[][][][][][]")

        lblDefaultAiSpeed.setFontSize(16)
        lblRowsToShow.setFontSize(16)
        nfLeaderboardSize.setFontSize(16)
        chckbxAiAutomaticallyFinish.setFontSize(16)
        chckbxCheckForUpdates.setFontSize(16)
        chckbxShowAnimations.setFontSize(16)

        panelCenter.add(lblDefaultAiSpeed, "cell 0 0")
        panelCenter.add(slider, "cell 1 0")
        panelCenter.add(lblRowsToShow, "cell 0 1,alignx leading")
        panelCenter.add(nfLeaderboardSize, "cell 1 1,alignx leading")
        panelCenter.add(chckbxAiAutomaticallyFinish, "flowx,cell 0 2")
        panelCenter.add(chckbxCheckForUpdates, "flowx,cell 0 3")
        panelCenter.add(chckbxShowAnimations, "cell 0 4")

        slider.addChangeListener(this)
        nfLeaderboardSize.addPropertyChangeListener(this)
        chckbxAiAutomaticallyFinish.addActionListener(this)
        chckbxCheckForUpdates.addActionListener(this)
        chckbxShowAnimations.addActionListener(this)
    }

    override fun refreshImpl(useDefaults: Boolean) {
        slider.value = preferenceService.get(Preferences.aiSpeed, useDefaults)
        nfLeaderboardSize.value = preferenceService.get(Preferences.leaderboardSize, useDefaults)
        chckbxAiAutomaticallyFinish.isSelected =
            preferenceService.get(Preferences.aiAutoContinue, useDefaults)
        chckbxCheckForUpdates.isSelected =
            preferenceService.get(Preferences.checkForUpdates, useDefaults)
        chckbxShowAnimations.isSelected =
            preferenceService.get(Preferences.showAnimations, useDefaults)
    }

    override fun saveImpl() {
        val aiSpd = slider.value
        preferenceService.save(Preferences.aiSpeed, aiSpd)

        val leaderboardSize = nfLeaderboardSize.getNumber()
        preferenceService.save(Preferences.leaderboardSize, leaderboardSize)

        val aiAuto = chckbxAiAutomaticallyFinish.isSelected
        preferenceService.save(Preferences.aiAutoContinue, aiAuto)

        val checkForUpdates = chckbxCheckForUpdates.isSelected
        preferenceService.save(Preferences.checkForUpdates, checkForUpdates)

        val showAnimations = chckbxShowAnimations.isSelected
        preferenceService.save(Preferences.showAnimations, showAnimations)
    }

    override fun hasOutstandingChanges() =
        slider.value != preferenceService.get(Preferences.aiSpeed) ||
            nfLeaderboardSize.value != preferenceService.get(Preferences.leaderboardSize) ||
            chckbxAiAutomaticallyFinish.isSelected !=
                preferenceService.get(Preferences.aiAutoContinue) ||
            chckbxCheckForUpdates.isSelected !=
                preferenceService.get(Preferences.checkForUpdates) ||
            chckbxShowAnimations.isSelected != preferenceService.get(Preferences.showAnimations)

    override fun stateChanged(e: ChangeEvent?) = stateChanged()

    override fun propertyChange(evt: PropertyChangeEvent?) = stateChanged()

    override fun actionPerformed(e: ActionEvent?) {
        stateChanged()
    }
}
