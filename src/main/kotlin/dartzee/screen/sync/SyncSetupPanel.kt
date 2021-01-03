package dartzee.screen.sync

import dartzee.sync.SyncMode
import dartzee.sync.validateSyncAction
import dartzee.utils.InjectedThings.syncConfigurer
import dartzee.utils.InjectedThings.syncManager
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class SyncSetupPanel: JPanel(), ActionListener
{
    private val instructionsPanel = JPanel()
    private val syncPanelOne = JLabel()
    private val syncPanelTwo = JLabel()
    private val syncPanelThree = JLabel()
    private val syncPanelFour = JLabel()

    private val setupPanel = JPanel()
    private val btnSetUp = JButton("Set Up")

    init
    {
        layout = BorderLayout(0, 0)
        add(instructionsPanel, BorderLayout.CENTER)

        syncPanelOne.preferredSize = Dimension(234, 148)
        syncPanelOne.size = Dimension(234, 148)
        syncPanelOne.icon = ImageIcon(javaClass.getResource("/sync/sync-step-1.png"))

        syncPanelTwo.preferredSize = Dimension(234, 148)
        syncPanelTwo.size = Dimension(234, 148)
        syncPanelTwo.icon = ImageIcon(javaClass.getResource("/sync/sync-step-2.png"))

        syncPanelThree.preferredSize = Dimension(234, 148)
        syncPanelThree.size = Dimension(234, 148)
        syncPanelThree.icon = ImageIcon(javaClass.getResource("/sync/sync-step-3.png"))

        syncPanelFour.preferredSize = Dimension(234, 148)
        syncPanelFour.size = Dimension(234, 148)
        syncPanelFour.icon = ImageIcon(javaClass.getResource("/sync/sync-step-4.png"))

        instructionsPanel.add(syncPanelOne)
        instructionsPanel.add(syncPanelTwo)
        instructionsPanel.add(syncPanelThree)
        instructionsPanel.add(syncPanelFour)

        add(setupPanel, BorderLayout.SOUTH)
        setupPanel.add(btnSetUp)

        btnSetUp.font = Font("Tahoma", Font.PLAIN, 18)
        btnSetUp.preferredSize = Dimension(200, 100)
        btnSetUp.addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        setUpPressed()
    }

    private fun setUpPressed()
    {
        if (!validateSyncAction())
        {
            return
        }

        val result = syncConfigurer.doFirstTimeSetup() ?: return
        when (result.mode)
        {
            SyncMode.CREATE_REMOTE -> syncManager.doPush(result.remoteName)
            SyncMode.OVERWRITE_LOCAL -> syncManager.doPull(result.remoteName)
            SyncMode.NORMAL_SYNC -> syncManager.doSync(result.remoteName)
        }
    }
}