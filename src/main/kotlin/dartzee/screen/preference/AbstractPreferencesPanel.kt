package dartzee.screen.preference

import dartzee.core.util.setFontSize
import dartzee.preferences.Preference
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.preferenceService
import dartzee.utils.resetCachedDartboardValues
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel

abstract class AbstractPreferencesPanel : JPanel() {
    abstract val title: String

    private val panelOptions = JPanel()
    private val btnApply = JButton("Apply")
    private val btnRestoreDefaults = JButton("Restore Defaults")

    init {
        layout = BorderLayout(0, 0)
        add(panelOptions, BorderLayout.SOUTH)
        panelOptions.add(btnApply)
        panelOptions.add(btnRestoreDefaults)

        btnApply.setFontSize(18)
        btnRestoreDefaults.setFontSize(18)

        val listener = PreferencesPanelListener()
        btnApply.addActionListener(listener)
        btnRestoreDefaults.addActionListener(listener)
    }

    fun refresh(useDefaults: Boolean) {
        refreshImpl(useDefaults)
        stateChanged()
    }

    fun stateChanged() {
        btnApply.isEnabled = hasOutstandingChanges()
    }

    protected fun JCheckBox.matchesPreference(preference: Preference<Boolean>) =
        isSelected == preferenceService.get(preference)

    inner class PreferencesPanelListener : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            when (e?.source) {
                btnApply -> save()
                btnRestoreDefaults -> refresh(true)
            }
        }

        private fun save() {
            saveImpl()
            stateChanged()

            // Refresh all active screens in case we've changed appearance preferences
            resetCachedDartboardValues()

            ScreenCache.fireAppearancePreferencesChanged()
        }
    }

    /**
     * Refresh this panel
     *
     * @param useDefaults: If true, the panel will refresh using the default values of its
     *   preferences.
     */
    abstract fun refreshImpl(useDefaults: Boolean)

    abstract fun saveImpl()

    abstract fun hasOutstandingChanges(): Boolean
}
