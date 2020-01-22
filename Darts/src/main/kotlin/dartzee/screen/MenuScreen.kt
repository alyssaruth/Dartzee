package dartzee.screen

import dartzee.screen.reporting.ReportingSetupScreen
import dartzee.screen.stats.overall.LeaderboardsScreen
import dartzee.core.screen.BugReportDialog
import dartzee.core.util.getAllChildComponentsForType
import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.ActionEvent
import javax.swing.AbstractButton
import javax.swing.JButton
import javax.swing.JPanel

class MenuScreen : EmbeddedScreen()
{
    private val menuDartboard = Dartboard(400, 400)

    private val btnNewGame = JButton("New Game")
    private val btnManagePlayers = JButton("Manage Players")
    private val btnGameStats = JButton("Leaderboards")
    private val btnPreferences = JButton("Preferences")
    private val btnAbout = JButton("About...")
    private val btnBugReport = JButton("Bug Report...")
    private val btnUtilities = JButton("Utilities")
    private val btnExit = JButton("Exit")
    private val btnGameReport = JButton("Game Report")

    init
    {
        val panel = JPanel()
        add(panel, BorderLayout.CENTER)
        panel.layout = null
        btnNewGame.font = Font("Tahoma", Font.PLAIN, 18)

        btnNewGame.setBounds(145, 40, 150, 50)
        panel.add(btnNewGame)
        btnManagePlayers.font = Font("Tahoma", Font.PLAIN, 18)
        btnManagePlayers.setBounds(60, 140, 150, 50)
        panel.add(btnManagePlayers)
        btnGameStats.font = Font("Tahoma", Font.PLAIN, 18)
        btnGameStats.setBounds(35, 240, 150, 50)
        panel.add(btnGameStats)
        btnPreferences.font = Font("Tahoma", Font.PLAIN, 18)
        btnPreferences.setBounds(505, 40, 150, 50)
        panel.add(btnPreferences)
        btnAbout.font = Font("Tahoma", Font.PLAIN, 18)
        btnAbout.setBounds(590, 140, 150, 50)
        panel.add(btnAbout)
        btnBugReport.font = Font("Tahoma", Font.PLAIN, 18)
        btnBugReport.setBounds(615, 240, 150, 50)
        panel.add(btnBugReport)
        btnExit.font = Font("Tahoma", Font.PLAIN, 18)
        btnExit.setBounds(325, 465, 150, 50)
        panel.add(btnExit)
        btnGameReport.font = Font("Tahoma", Font.PLAIN, 18)
        btnGameReport.setBounds(60, 340, 150, 50)
        panel.add(btnGameReport)
        btnUtilities.font = Font("Tahoma", Font.PLAIN, 18)
        btnUtilities.setBounds(590, 340, 150, 50)
        panel.add(btnUtilities)

        menuDartboard.setBounds(200, 65, 400, 400)
        menuDartboard.paintDartboard(null, false)
        panel.add(menuDartboard)

        //Add ActionListeners
        val buttons = getAllChildComponentsForType(this, AbstractButton::class.java)
        for (button in buttons)
        {
            button.addActionListener(this)
        }
    }

    override fun getScreenName(): String
    {
        return "Menu"
    }

    override fun initialise()
    {
        //Do nothing
    }

    override fun showBackButton(): Boolean
    {
        //This is the root screen!
        return false
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        val src = arg0.source as AbstractButton

        when (src)
        {
            btnAbout -> {
                val dialog = AboutDialogDarts()
                dialog.setLocationRelativeTo(this)
                dialog.isModal = true
                dialog.isVisible = true
            }

            btnBugReport -> {
                val dialog = BugReportDialog()
                dialog.setLocationRelativeTo(this)
                dialog.isVisible = true
            }

            btnPreferences -> {
                val dialog = ScreenCache.getPreferencesDialog()
                dialog.setLocationRelativeTo(this)
                dialog.isVisible = true
            }

            btnExit -> ScreenCache.exitApplication()
            btnNewGame -> ScreenCache.switchScreen(GameSetupScreen::class.java)
            btnManagePlayers -> ScreenCache.switchScreen(PlayerManagementScreen::class.java)
            btnGameReport -> ScreenCache.switchScreen(ReportingSetupScreen::class.java)
            btnGameStats -> ScreenCache.switchScreen(LeaderboardsScreen())
            btnUtilities -> ScreenCache.switchScreen(UtilitiesScreen::class.java)
            else -> super.actionPerformed(arg0)
        }
    }
}
