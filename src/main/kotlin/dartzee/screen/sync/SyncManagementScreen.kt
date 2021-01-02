package dartzee.screen.sync

import dartzee.screen.EmbeddedScreen
import dartzee.screen.ScreenCache
import dartzee.sync.getRemoteName
import java.awt.BorderLayout

class SyncManagementScreen: EmbeddedScreen()
{
    private val setupPanel = SyncSetupPanel()
    private val managementPanel = SyncManagementPanel()

    override fun initialise()
    {
        remove(setupPanel)
        remove(managementPanel)

        val remoteName = getRemoteName()
        if (remoteName.isEmpty())
        {
            add(setupPanel, BorderLayout.CENTER)
        }
        else
        {
            add(managementPanel, BorderLayout.CENTER)
        }

        ScreenCache.mainScreen.pack()
        repaint()
    }

    override fun getScreenName() = "Sync Management"
}