package dartzee.screen.preference

import dartzee.core.util.setFontSize
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JPanel

abstract class AbstractPreferencesPanel : JPanel()
{
    private val panelOptions = JPanel()
    private val btnApply = JButton("Apply")
    private val btnRestoreDefaults = JButton("Restore Defaults")

    init
    {
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

    inner class PreferencesPanelListener: ActionListener
    {
        override fun actionPerformed(e: ActionEvent?)
        {
            when (e?.source)
            {
                btnApply -> save()
                btnRestoreDefaults -> refresh(true)
            }
        }
    }

    /**
     * Refresh this panel
     *
     * @param useDefaults: If true, the panel will refresh using the default values of its preferences.
     */
    abstract fun refresh(useDefaults: Boolean)
    abstract fun save()
}
