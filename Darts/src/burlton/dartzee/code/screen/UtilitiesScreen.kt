package burlton.dartzee.code.screen

import burlton.core.code.util.dumpThreadStacks
import burlton.dartzee.code.db.sanity.DatabaseSanityCheck
import burlton.dartzee.code.utils.DARTS_VERSION_NUMBER
import burlton.dartzee.code.utils.DartsDatabaseUtil
import burlton.dartzee.code.utils.DevUtilities
import burlton.dartzee.code.utils.UpdateManager
import burlton.desktopcore.code.util.getAllChildComponentsForType
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.ActionEvent
import javax.swing.AbstractButton
import javax.swing.JButton
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
            btnCheckForUpdates -> UpdateManager.checkForUpdates(DARTS_VERSION_NUMBER)
            btnViewLogs -> {val loggingDialog = ScreenCache.getDebugConsole()
                            loggingDialog.isVisible = true
                            loggingDialog.toFront()}
            btnThreadStacks -> dumpThreadStacks()
            btnAchievementConversion -> runAchievementConversion()
            else -> super.actionPerformed(arg0)
        }
    }

    private fun runAchievementConversion()
    {
        val dlg = AchievementConversionDialog()
        dlg.setLocationRelativeTo(ScreenCache.getMainScreen())
        dlg.isVisible = true
    }

    override fun getScreenName(): String
    {
        return "Utilities"
    }
}
