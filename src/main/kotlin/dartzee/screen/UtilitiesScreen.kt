package dartzee.screen

import dartzee.`object`.DartsClient
import dartzee.core.util.DialogUtil
import dartzee.core.util.dumpThreadStacks
import dartzee.core.util.getAllChildComponentsForType
import dartzee.db.sanity.DatabaseSanityCheck
import dartzee.screen.dartzee.DartzeeTemplateSetupScreen
import dartzee.utils.*
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.ActionEvent
import javax.swing.AbstractButton
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JPanel

class UtilitiesScreen : EmbeddedScreen()
{
    private val btnDeleteGame = JButton("Delete Game")
    private val btnCreateBackup = JButton("Create backup")
    private val btnRestoreFromBackup = JButton("Restore from backup")
    private val btnPerformDatabaseCheck = JButton("Perform Database Check")
    private val btnCheckForUpdates = JButton("Check for Updates")
    private val btnViewLogs = JButton("View Logs")
    private val btnThreadStacks = JButton("Thread Stacks")
    private val btnAchievementConversion = JButton("Run Achievement Conversion")
    private val btnSetLogSecret = JButton("Enable emailing of logs")
    private val btnDartzeeTemplates = JButton("Dartzee Templates")

    init
    {
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
        panel.add(btnSetLogSecret, "cell 0 12, alignx center")
        panel.add(btnDartzeeTemplates, "cell 0 13, alignx center")

        val buttons = getAllChildComponentsForType(panel, AbstractButton::class.java)
        for (button in buttons)
        {
            button.font = Font("Tahoma", Font.PLAIN, 18)
            button.addActionListener(this)
        }
    }

    override fun initialise()
    {
        //Nothing to do, it's just a placeholder for some buttons
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnDeleteGame -> DevUtilities.purgeGame()
            btnCreateBackup -> DartsDatabaseUtil.backupCurrentDatabase()
            btnRestoreFromBackup -> DartsDatabaseUtil.restoreDatabase()
            btnPerformDatabaseCheck -> DatabaseSanityCheck.runSanityCheck()
            btnCheckForUpdates -> DartsClient.updateManager.checkForUpdates(DARTS_VERSION_NUMBER)
            btnViewLogs -> {val loggingDialog = ScreenCache.debugConsole
                            loggingDialog.isVisible = true
                            loggingDialog.toFront()}
            btnThreadStacks -> dumpThreadStacks()
            btnAchievementConversion -> runAchievementConversion()
            btnSetLogSecret -> setLogSecret()
            btnDartzeeTemplates -> ScreenCache.switchScreen(DartzeeTemplateSetupScreen::class.java)
            else -> super.actionPerformed(arg0)
        }
    }

    private fun setLogSecret()
    {
        val pwd = JOptionPane.showInputDialog(null, "Please enter the debugging password.", "Password")
        if (pwd.isNullOrEmpty())
        {
            return
        }

        PreferenceUtil.saveString(PREFERENCES_STRING_LOG_SECRET, pwd)
        DartsClient.logSecret = pwd

        val success = ClientEmailer.sendClientEmail("Log secret test", "Secret set successfully")
        if (success)
        {
            DialogUtil.showInfo("Test log sent successfully!")
        }
        else
        {
            DialogUtil.showError("Sending a test log failed. Check you entered the password correctly or let me know!")
        }
    }

    private fun runAchievementConversion()
    {
        val dlg = AchievementConversionDialog()
        dlg.setLocationRelativeTo(ScreenCache.mainScreen)
        dlg.isVisible = true
    }

    override fun getScreenName() = "Utilities"
}
