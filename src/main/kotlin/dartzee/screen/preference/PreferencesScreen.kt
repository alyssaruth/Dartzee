package dartzee.screen.preference

import dartzee.core.util.getAllChildComponentsForType
import dartzee.screen.EmbeddedScreen
import java.awt.BorderLayout
import javax.swing.JTabbedPane
import javax.swing.SwingConstants

class PreferencesScreen: EmbeddedScreen()
{
    val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val dartboardTab = PreferencesPanelDartboard()
    private val scorerTab = PreferencesPanelScorer()
    private val miscTab = PreferencesPanelMisc()

    init
    {
        add(tabbedPane, BorderLayout.CENTER)
        tabbedPane.addTab("Dartboard", dartboardTab)
        tabbedPane.addTab("Scorer", scorerTab)
        tabbedPane.addTab("Misc", miscTab)
    }

    override fun initialise()
    {
        getAllChildComponentsForType<AbstractPreferencesPanel>().forEach{
            it.refresh(false)
        }
    }

    override fun getScreenName() = "Preferences"
}