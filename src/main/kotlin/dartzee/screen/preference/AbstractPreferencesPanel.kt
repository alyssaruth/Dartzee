package dartzee.screen.preference

import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JPanel

abstract class AbstractPreferencesPanel : JPanel(), ActionListener
{
    val btnApply = JButton("Apply")
    val btnRestoreDefaults = JButton("Restore Defaults")

    init
    {
        btnApply.addActionListener(this)
        btnRestoreDefaults.addActionListener(this)
    }


    /**
     * Refresh this panel
     *
     * @param useDefaults: If true, the panel will refresh using the default values of its preferences.
     */
    abstract fun refresh(useDefaults: Boolean)
    abstract fun save()
}
