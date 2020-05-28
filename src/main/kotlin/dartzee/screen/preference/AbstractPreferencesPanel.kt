package dartzee.screen.preference

import javax.swing.JPanel

abstract class AbstractPreferencesPanel : JPanel()
{
    /**
     * Refresh this panel
     *
     * @param useDefaults: If true, the panel will refresh using the default values of its preferences.
     */
    abstract fun refresh(useDefaults: Boolean)

    abstract fun save()
}
