package dartzee.screen.sync

import dartzee.core.util.DialogUtil
import dartzee.core.util.getAllChildComponentsForType
import dartzee.db.SyncAuditEntity
import dartzee.screen.EmbeddedScreen
import dartzee.screen.ScreenCache
import dartzee.sync.*
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.InjectedThings.remoteDatabaseStore
import dartzee.utils.ResourceCache.BASE_FONT
import net.miginfocom.swing.MigLayout
import java.awt.*
import java.awt.event.ActionEvent
import java.io.File
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.border.TitledBorder.DEFAULT_POSITION
import javax.swing.border.TitledBorder.LEADING

class SyncManagementScreen: EmbeddedScreen()
{
    private val lblSharedDbName = JLabel("Shared database")
    private val tfRemoteName = TextField()
    private val btnSetUp = JButton("Set Up")
    private val btnPerformSync = JButton("Perform Sync")
    private val panelCenter = JPanel()
    private val panelMainOptions = JPanel()
    private val panelOtherOptions = JPanel()
    private val btnPush = JButton("Push")
    private val btnPull = JButton("Pull")
    private val btnReset = JButton("Reset")

    init
    {
        add(panelCenter, BorderLayout.CENTER)
        panelCenter.layout = BorderLayout(0, 0)
        panelCenter.add(panelMainOptions, BorderLayout.CENTER)
        val panelDbName = JPanel()
        panelDbName.add(lblSharedDbName)
        panelDbName.add(tfRemoteName)
        val panelSetUpAndSync = JPanel()
        panelSetUpAndSync.add(btnSetUp)
        panelSetUpAndSync.add(btnPerformSync)
        panelMainOptions.layout = MigLayout("", "[grow]", "[][][]")
        panelMainOptions.add(panelDbName, "cell 0 0, alignx center,aligny center")
        panelMainOptions.add(panelSetUpAndSync, "cell 0 1, alignx center, aligny center")

        panelCenter.add(panelOtherOptions, BorderLayout.SOUTH)
        panelOtherOptions.layout = MigLayout("", "[grow]", "[][][]")
        val panelPushPull = JPanel()
        panelPushPull.add(btnPush)
        panelPushPull.add(btnPull)
        panelOtherOptions.add(panelPushPull, "cell 0 0,alignx center,aligny center")
        panelOtherOptions.add(btnReset, "cell 0 1,alignx center,aligny center")
        panelOtherOptions.border = TitledBorder(null, "Other options", LEADING, DEFAULT_POSITION, BASE_FONT.deriveFont(Font.PLAIN, 24f))

        val buttons = panelCenter.getAllChildComponentsForType<AbstractButton>()
        for (button in buttons)
        {
            button.font = Font("Tahoma", Font.PLAIN, 18)
            button.preferredSize = Dimension(200, 100)
            button.addActionListener(this)
        }

        btnReset.background = Color.RED.brighter().brighter()
        btnReset.foreground = Color.RED

        tfRemoteName.columns = 20
        tfRemoteName.isEditable = false
        tfRemoteName.isFocusable = false
    }

    override fun initialise()
    {
        val remoteName = getRemoteName()
        tfRemoteName.text = remoteName
        btnReset.isEnabled = remoteName.isNotEmpty()
        btnPerformSync.isEnabled = remoteName.isNotEmpty()
        btnPush.isEnabled = remoteName.isNotEmpty()
        btnPull.isEnabled = remoteName.isNotEmpty()
        btnSetUp.isEnabled = remoteName.isEmpty()
    }

    override fun getScreenName() = "Sync Management"

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnSetUp -> setUpPressed()
            btnReset -> resetPressed()
            btnPerformSync -> syncPressed()
            btnPush -> pushPressed()
            btnPull -> pullPressed()
            else -> super.actionPerformed(arg0)
        }
    }

    private fun setUpPressed()
    {
        if (!validateSyncAction())
        {
            return
        }

        val result = InjectedThings.syncConfigurer.doFirstTimeSetup() ?: return
        when (result.mode)
        {
            SyncMode.CREATE_REMOTE -> pushPressed(false)
            SyncMode.OVERWRITE_LOCAL -> pullPressed(false)
            SyncMode.NORMAL_SYNC -> syncPressed(false)
        }

        saveRemoteName(result.remoteName)
        initialise()
    }

    private fun pushPressed(doValidation: Boolean = true)
    {
        if (doValidation && !validateSyncAction())
        {
            return
        }

        val remoteName = getRemoteName()
        SyncAuditEntity.insertSyncAudit(mainDatabase, remoteName)
        remoteDatabaseStore.pushDatabase(remoteName, mainDatabase)
    }

    private fun pullPressed(doValidation: Boolean = true)
    {
        if (doValidation && !validateSyncAction())
        {
            return
        }

        val remoteName = getRemoteName()
        val remote = remoteDatabaseStore.fetchDatabase(remoteName).database
        SyncAuditEntity.insertSyncAudit(remote, remoteName)
        DartsDatabaseUtil.swapInDatabase(File(remote.filePath))
    }

    private fun syncPressed(doValidation: Boolean = true)
    {
        if (doValidation && !validateSyncAction())
        {
            return
        }

        val manager = SyncManager(getRemoteName(), remoteDatabaseStore)
        manager.doSync()

        initialise()
    }

    private fun validateSyncAction(): Boolean
    {
        val openScreens = ScreenCache.getDartsGameScreens()
        if (openScreens.isNotEmpty())
        {
            DialogUtil.showError("You must close all open games before performing this action.")
            return false
        }

        return true
    }

    private fun resetPressed()
    {
        val remoteName = getRemoteName()
        val q = "Are you sure you want to reset?\n\nThis will not delete any local data, but will sever the link with $remoteName, requiring you to set it up again."
        val answer = DialogUtil.showQuestion(q)
        if (answer == JOptionPane.YES_OPTION)
        {
            resetRemote()
            initialise()
        }
    }
}