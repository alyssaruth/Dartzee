package dartzee.screen

import dartzee.core.util.dumpThreadStacks
import dartzee.core.util.getAllChildComponentsForType
import dartzee.core.util.runInOtherThread
import dartzee.db.sanity.DatabaseSanityCheck
import dartzee.logging.CODE_PARTY_MODE
import dartzee.`object`.DartsClient
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.DevUtilities
import dartzee.utils.InjectedThings
import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.ActionEvent
import javax.swing.AbstractButton
import javax.swing.JButton
import javax.swing.JPanel
import net.miginfocom.swing.MigLayout

class UtilitiesScreen : EmbeddedScreen() {
    private val btnDeleteGame = JButton("Delete Game")
    private val btnCreateBackup = JButton("Create backup")
    private val btnRestoreFromBackup = JButton("Restore from backup")
    private val btnPerformDatabaseCheck = JButton("Perform Database Check")
    private val btnCheckForUpdates = JButton("Check for Updates")
    private val btnViewLogs = JButton("View Logs")
    private val btnThreadStacks = JButton("Thread Stacks")
    private val btnAchievementConversion = JButton("Run Achievement Conversion")
    private val btnPartyMode = JButton("Enter Party Mode")

    init {
        val panel = JPanel()
        add(panel, BorderLayout.CENTER)
        panel.layout = MigLayout("", "[grow]", "[][][][][][][][][][][]")
        panel.add(btnDeleteGame, "cell 0 0,alignx center,aligny center")
        panel.add(btnCreateBackup, "cell 0 2,alignx center")
        panel.add(btnRestoreFromBackup, "cell 0 4,alignx center")
        panel.add(btnPerformDatabaseCheck, "cell 0 6,alignx center,aligny center")
        panel.add(btnThreadStacks, "cell 0 7,alignx center")
        panel.add(btnCheckForUpdates, "cell 0 8,alignx center")
        panel.add(btnViewLogs, "cell 0 10,alignx center")
        panel.add(btnAchievementConversion, "cell 0 11,alignx center")
        panel.add(btnPartyMode, "cell 0 12,alignx center")

        val buttons = panel.getAllChildComponentsForType<AbstractButton>()
        for (button in buttons) {
            button.font = Font("Tahoma", Font.PLAIN, 18)
            button.addActionListener(this)
        }
    }

    override fun initialise() {
        // Nothing to do, it's just a placeholder for some buttons
    }

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source) {
            btnDeleteGame -> DevUtilities.purgeGame()
            btnCreateBackup -> DartsDatabaseUtil.backupCurrentDatabase()
            btnRestoreFromBackup -> DartsDatabaseUtil.restoreDatabase()
            btnPerformDatabaseCheck -> DatabaseSanityCheck.runSanityCheck()
            btnCheckForUpdates ->
                runInOtherThread { DartsClient.updateManager.checkForUpdates(DARTS_VERSION_NUMBER) }
            btnViewLogs -> {
                val loggingDialog = InjectedThings.loggingConsole
                loggingDialog.isVisible = true
                loggingDialog.toFront()
            }
            btnThreadStacks -> dumpThreadStacks()
            btnAchievementConversion -> runAchievementConversion()
            btnPartyMode -> enterPartyMode()
            else -> super.actionPerformed(arg0)
        }
    }

    private fun enterPartyMode() {
        InjectedThings.logger.info(CODE_PARTY_MODE, "Entering party mode!")
        InjectedThings.partyMode = true

        ScreenCache.switch<MenuScreen>()
    }

    private fun runAchievementConversion() {
        val dlg = AchievementConversionDialog()
        dlg.setLocationRelativeTo(ScreenCache.mainScreen)
        dlg.isVisible = true
    }

    override fun getScreenName() = "Utilities"
}
