package dartzee.screen.sync

import dartzee.core.util.DialogUtil
import dartzee.core.util.formatTimestamp
import dartzee.core.util.getAllChildComponentsForType
import dartzee.core.util.setFontSize
import dartzee.sync.LastSyncData
import dartzee.sync.getModifiedGameCount
import dartzee.sync.resetRemote
import dartzee.sync.validateSyncAction
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.sql.Timestamp
import java.time.Duration
import javax.swing.AbstractButton
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder
import net.miginfocom.swing.MigLayout

class SyncManagementPanel : JPanel(), ActionListener {
    private var remoteName = ""

    private val btnPerformSync = JButton("Perform Sync")
    private val panelSyncStatus = JPanel()
    private val lblSharedDatabaseName = JLabel("")
    private val lblLastSynced = JLabel("")
    private val lblPendingGames = JLabel("")
    private val panelMainOptions = JPanel()
    private val panelOtherOptions = JPanel()
    private val btnPush = JButton("Push")
    private val btnPull = JButton("Pull")
    private val btnReset = JButton("Reset")

    init {
        layout = BorderLayout(0, 0)
        add(panelMainOptions, BorderLayout.CENTER)
        btnPerformSync.icon = ImageIcon(javaClass.getResource("/buttons/sync.png"))
        btnReset.icon = ImageIcon(javaClass.getResource("/buttons/Reset.png"))
        btnPull.icon = ImageIcon(javaClass.getResource("/buttons/pull.png"))
        btnPush.icon = ImageIcon(javaClass.getResource("/buttons/push.png"))

        panelSyncStatus.background = Color.WHITE
        panelSyncStatus.border = LineBorder(Color.BLACK, 3)
        lblSharedDatabaseName.setFontSize(14)
        lblPendingGames.setFontSize(14)
        lblLastSynced.setFontSize(14)

        panelSyncStatus.layout = MigLayout("", "[grow]", "[][][]")
        panelSyncStatus.add(lblSharedDatabaseName, "cell 0 0, alignx center,aligny center")
        panelSyncStatus.add(lblLastSynced, "cell 0 1, alignx center,aligny center")
        panelSyncStatus.add(lblPendingGames, "cell 0 2, alignx center,aligny center")

        val panelSetUpAndSync = JPanel()
        panelSetUpAndSync.add(btnPerformSync)
        panelMainOptions.layout = MigLayout("", "[grow]", "[][][]")
        panelMainOptions.add(panelSyncStatus, "cell 0 0, alignx center,aligny center")
        panelMainOptions.add(panelSetUpAndSync, "cell 0 1, alignx center, aligny center")

        add(panelOtherOptions, BorderLayout.SOUTH)
        panelOtherOptions.layout = MigLayout("", "[grow]", "[][][]")
        val panelPushPull = JPanel()
        panelPushPull.add(btnPush)
        panelPushPull.add(btnPull)
        panelOtherOptions.add(panelPushPull, "cell 0 0,alignx center,aligny center")
        panelOtherOptions.add(btnReset, "cell 0 1,alignx center,aligny center")
        panelOtherOptions.border =
            TitledBorder(
                null,
                "Other options",
                TitledBorder.LEADING,
                TitledBorder.DEFAULT_POSITION,
                ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 20f)
            )

        val buttons = getAllChildComponentsForType<AbstractButton>()
        for (button in buttons) {
            button.font = Font("Tahoma", Font.PLAIN, 18)
            button.preferredSize = Dimension(200, 100)
            button.addActionListener(this)
        }

        btnReset.foreground = Color.RED.darker()
    }

    fun updateStatus(syncData: LastSyncData) {
        val pendingGameCount = getModifiedGameCount()

        remoteName = syncData.remoteName

        lblSharedDatabaseName.text = "<html><b>Shared Database:</b> $remoteName</html>"
        lblLastSynced.text =
            "<html><font color=\"${getColour(syncData.lastSynced)}\"><b>Last Synced:</b> ${syncData.lastSynced.formatTimestamp()}</font></html>"
        lblPendingGames.text =
            "<html><font color=\"${getColourForGameCount(pendingGameCount)}\"><b>Pending Games:</b> $pendingGameCount</font></html>"
    }

    private fun getColourForGameCount(pendingGameCount: Int) =
        when {
            pendingGameCount >= 10 -> "red"
            pendingGameCount >= 1 -> "orange"
            else -> "green"
        }

    private fun getColour(lastSynced: Timestamp): String {
        val currentTime = InjectedThings.clock.instant()

        val diff = Duration.between(lastSynced.toInstant(), currentTime)
        return when {
            diff.toDays() > 7 -> "red"
            diff.toHours() > 24 -> "orange"
            else -> "green"
        }
    }

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source) {
            btnReset -> resetPressed()
            btnPerformSync -> syncPressed()
            btnPush -> pushPressed()
            btnPull -> pullPressed()
        }
    }

    private fun pushPressed() {
        if (!validateSyncAction()) {
            return
        }

        if (InjectedThings.syncManager.databaseExists(remoteName)) {
            val q =
                "Are you sure you want to push to $remoteName? \n\nThis will overwrite any data that hasn't been synced to this device."
            val ans = DialogUtil.showQuestionOLD(q)
            if (ans != JOptionPane.YES_OPTION) {
                return
            }
        }

        InjectedThings.syncManager.doPush(remoteName)
    }

    private fun pullPressed() {
        if (!validateSyncAction()) {
            return
        }

        val q =
            "Are you sure you want to pull from $remoteName? \n\nThis will overwrite any local data that hasn't been synced to $remoteName from this device."
        val ans = DialogUtil.showQuestionOLD(q)
        if (ans != JOptionPane.YES_OPTION) {
            return
        }

        InjectedThings.syncManager.doPull(remoteName)
    }

    private fun syncPressed() {
        if (!validateSyncAction()) {
            return
        }

        InjectedThings.syncManager.doSyncIfNecessary(remoteName)
    }

    private fun resetPressed() {
        val q =
            "Are you sure you want to reset?\n\nThis will not delete any local data, but will sever the link with $remoteName, requiring you to set it up again."
        val answer = DialogUtil.showQuestionOLD(q)
        if (answer == JOptionPane.YES_OPTION) {
            resetRemote()
        }
    }
}
