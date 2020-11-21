package dartzee.screen.sync

import dartzee.core.util.addActionListenerToAllChildren
import dartzee.screen.EmbeddedScreen
import dartzee.sync.*
import java.awt.BorderLayout
import java.awt.TextField
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class SyncManagementScreen: EmbeddedScreen()
{
    private val lblRemoteName = TextField()
    private val btnReset = JButton("Reset")
    private val btnPerformSync = JButton("Perform Sync")
    private val panelCenter= JPanel()

    init
    {
        add(panelCenter, BorderLayout.CENTER)
        lblRemoteName.isEditable = false

        val panelRemote = JPanel()
        panelRemote.add(JLabel("Syncing with"))
        panelRemote.add(lblRemoteName)
        panelRemote.add(btnReset)

        panelCenter.add(panelRemote)
        panelCenter.add(btnPerformSync)

        addActionListenerToAllChildren(this)
    }

    override fun initialise()
    {
        val remoteName = getRemoteName()
        lblRemoteName.text = remoteName
        btnReset.isEnabled = remoteName.isNotEmpty()
    }

    override fun getScreenName() = "Sync Management"

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnReset -> resetRemote()
            btnPerformSync -> doSync()
            else -> super.actionPerformed(arg0)
        }
    }

    private fun resetRemote()
    {
        saveRemoteName("")
        initialise()
    }

    private fun doSync()
    {
        val store = AmazonS3RemoteDatabaseStore(SYNC_BUCKET_NAME)
        val config = SyncConfigurer(store).validateAndConfigureSync()
        if (config != null)
        {
            val manager = SyncManager(config.mode, config.remoteName, store)
            manager.doSync()

            initialise()
        }
    }
}