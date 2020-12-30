package dartzee.screen.sync

import dartzee.screen.EmbeddedScreen
import dartzee.sync.getRemoteName
import java.awt.BorderLayout

class SyncManagementScreen: EmbeddedScreen()
{
    private val setupPanel = SyncSetupPanel(this)
    private val managementPanel = SyncManagementPanel(this)

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

        repaint()
    }

    override fun getScreenName() = "Sync Management"
}