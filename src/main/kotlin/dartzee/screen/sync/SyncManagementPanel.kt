package dartzee.screen.sync

import dartzee.core.util.DialogUtil
import dartzee.core.util.getAllChildComponentsForType
import dartzee.screen.ScreenCache
import dartzee.sync.getRemoteName
import dartzee.sync.resetRemote
import dartzee.sync.validateSyncAction
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.AbstractButton
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.border.TitledBorder

class SyncManagementPanel: JPanel(), ActionListener
{
    private val btnPerformSync = JButton("Perform Sync")
    private val panelMainOptions = JPanel()
    private val panelOtherOptions = JPanel()
    private val btnPush = JButton("Push")
    private val btnPull = JButton("Pull")
    private val btnReset = JButton("Reset")

    init
    {
        layout = BorderLayout(0, 0)
        add(panelMainOptions, BorderLayout.CENTER)
        val panelDbName = JPanel()
        val panelSetUpAndSync = JPanel()
        panelSetUpAndSync.add(btnPerformSync)
        panelMainOptions.layout = MigLayout("", "[grow]", "[][][]")
        panelMainOptions.add(panelDbName, "cell 0 0, alignx center,aligny center")
        panelMainOptions.add(panelSetUpAndSync, "cell 0 1, alignx center, aligny center")

        add(panelOtherOptions, BorderLayout.SOUTH)
        panelOtherOptions.layout = MigLayout("", "[grow]", "[][][]")
        val panelPushPull = JPanel()
        panelPushPull.add(btnPush)
        panelPushPull.add(btnPull)
        panelOtherOptions.add(panelPushPull, "cell 0 0,alignx center,aligny center")
        panelOtherOptions.add(btnReset, "cell 0 1,alignx center,aligny center")
        panelOtherOptions.border = TitledBorder(null, "Other options", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 24f))

        val buttons = getAllChildComponentsForType<AbstractButton>()
        for (button in buttons)
        {
            button.font = Font("Tahoma", Font.PLAIN, 18)
            button.preferredSize = Dimension(200, 100)
            button.addActionListener(this)
        }
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnReset -> resetPressed()
            btnPerformSync -> syncPressed()
            btnPush -> pushPressed()
            btnPull -> pullPressed()
        }
    }

    private fun pushPressed()
    {
        if (!validateSyncAction())
        {
            return
        }

        val remoteName = getRemoteName()
        if (InjectedThings.remoteDatabaseStore.databaseExists(remoteName))
        {
            val q = "Are you sure you want to push to $remoteName? \n\nThis will overwrite any data that hasn't been synced to this device."
            val ans = DialogUtil.showQuestion(q)
            if (ans != JOptionPane.YES_OPTION)
            {
                return
            }
        }

        InjectedThings.syncManager.doPush(remoteName)
    }

    private fun pullPressed()
    {
        if (!validateSyncAction())
        {
            return
        }

        val remoteName = getRemoteName()
        val q = "Are you sure you want to pull from $remoteName? \n\nThis will overwrite any local data that hasn't been synced to $remoteName from this device."
        val ans = DialogUtil.showQuestion(q)
        if (ans != JOptionPane.YES_OPTION)
        {
            return
        }

        InjectedThings.syncManager.doPull(remoteName)
    }

    private fun syncPressed()
    {
        if (!validateSyncAction())
        {
            return
        }

        InjectedThings.syncManager.doSync(getRemoteName())
    }

    private fun resetPressed()
    {
        val remoteName = getRemoteName()
        val q = "Are you sure you want to reset?\n\nThis will not delete any local data, but will sever the link with $remoteName, requiring you to set it up again."
        val answer = DialogUtil.showQuestion(q)
        if (answer == JOptionPane.YES_OPTION)
        {
            resetRemote()
        }
    }
}