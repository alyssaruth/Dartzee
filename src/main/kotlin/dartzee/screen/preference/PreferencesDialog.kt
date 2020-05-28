package dartzee.screen.preference

import dartzee.core.util.getAllChildComponentsForType
import dartzee.logging.CODE_SWING_ERROR
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.logger
import dartzee.utils.resetCachedDartboardValues
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class PreferencesDialog : JDialog(), ActionListener
{
    val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val dartboardTab = PreferencesPanelDartboard()
    private val scorerTab = PreferencesPanelScorer()
    private val miscTab = PreferencesPanelMisc()

    //Ok/cancel etc
    val btnOk = JButton("Ok")
    val btnRestoreDefaults = JButton("Restore Defaults")
    val btnCancel = JButton("Cancel")

    init
    {
        setSize(500, 384)
        title = "Preferences"
        isResizable = false
        isModal = true

        contentPane.add(tabbedPane, BorderLayout.CENTER)
        tabbedPane.addTab("Dartboard", dartboardTab)
        tabbedPane.addTab("Scorer", scorerTab)
        tabbedPane.addTab("Misc", miscTab)

        val panel = JPanel()
        contentPane.add(panel, BorderLayout.SOUTH)
        panel.add(btnOk)
        panel.add(btnRestoreDefaults)
        panel.add(btnCancel)

        btnOk.addActionListener(this)
        btnRestoreDefaults.addActionListener(this)
        btnCancel.addActionListener(this)
    }

    fun init()
    {
        getPreferencePanels().forEach{
            it.refresh(false)
        }
    }

    private fun save()
    {
        //Tell all the panels to save
        getPreferencePanels().forEach{
            it.save()
        }

        //Refresh all active screens in case we've changed appearance preferences
        resetCachedDartboardValues()

        ScreenCache.getDartsGameScreens().forEach{
            it.fireAppearancePreferencesChanged()
        }
    }

    private fun resetPreferencesForSelectedTab()
    {
        val selectedTab = tabbedPane.selectedComponent
        if (selectedTab !is AbstractPreferencesPanel)
        {
            logger.error(CODE_SWING_ERROR, "Called 'restore defaults' on unexpected component: $selectedTab")
            return
        }

        selectedTab.refresh(true)
    }

    private fun getPreferencePanels() = getAllChildComponentsForType<AbstractPreferencesPanel>()

    override fun actionPerformed(arg0: ActionEvent)
    {
        val source = arg0.source as Component
        when (source)
        {
            btnOk ->
            {
                save()
                dispose()
            }
            btnRestoreDefaults -> resetPreferencesForSelectedTab()
            btnCancel -> dispose()
        }
    }
}
