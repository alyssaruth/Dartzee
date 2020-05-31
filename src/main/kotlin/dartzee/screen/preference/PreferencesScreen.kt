package dartzee.screen.preference

import dartzee.core.util.DialogUtil
import dartzee.screen.EmbeddedScreen
import java.awt.BorderLayout
import javax.swing.JOptionPane
import javax.swing.JTabbedPane
import javax.swing.SwingConstants

private fun getPreferenceTabs() = listOf(PreferencesPanelDartboard(), PreferencesPanelScorer(), PreferencesPanelMisc())

class PreferencesScreen(private val tabs: List<AbstractPreferencesPanel> = getPreferenceTabs()) : EmbeddedScreen()
{
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)

    init
    {
        add(tabbedPane, BorderLayout.CENTER)
        tabs.forEach {
            tabbedPane.addTab(it.title, it)
        }
    }

    override fun initialise()
    {
        tabs.forEach {
            it.refresh(false)
        }
    }

    override fun getScreenName() = "Preferences"

    override fun backPressed()
    {
        val outstandingChanges = tabs.any { it.hasOutstandingChanges() }
        if (outstandingChanges)
        {
            val ans = DialogUtil.showQuestion("Are you sure you want to go back?\n\nYou have unsaved changes that will be discarded.")
            if (ans != JOptionPane.YES_OPTION)
            {
                return
            }
        }

        super.backPressed()
    }
}