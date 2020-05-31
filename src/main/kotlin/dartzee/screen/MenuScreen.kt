package dartzee.screen

import dartzee.core.util.addActionListenerToAllChildren
import dartzee.main.exitApplication
import dartzee.screen.dartzee.DartzeeTemplateSetupScreen
import dartzee.screen.player.PlayerManagementScreen
import dartzee.screen.preference.PreferencesScreen
import dartzee.screen.reporting.ReportingSetupScreen
import dartzee.screen.stats.overall.LeaderboardsScreen
import dartzee.utils.ResourceCache
import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JPanel

class MenuScreen : EmbeddedScreen()
{
    private val menuDartboard = Dartboard(400, 400)

    private val btnNewGame = JButton("New Game")
    private val btnManagePlayers = JButton("Manage Players")
    private val btnLeaderboards = JButton("Leaderboards")
    private val btnPreferences = JButton("Preferences")
    private val btnAbout = JButton("About...")
    private val btnDartzeeTemplates = JButton("Dartzee")
    private val btnUtilities = JButton("Utilities")
    private val btnExit = JButton("Exit")
    private val btnGameReport = JButton("Game Report")

    private val buttonFont = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 18f)

    init
    {
        val panel = JPanel()
        add(panel, BorderLayout.CENTER)
        panel.layout = null
        btnNewGame.font = buttonFont

        btnNewGame.setBounds(145, 40, 150, 50)
        panel.add(btnNewGame)
        btnManagePlayers.font = buttonFont
        btnManagePlayers.setBounds(60, 140, 150, 50)
        panel.add(btnManagePlayers)
        btnLeaderboards.font = buttonFont
        btnLeaderboards.setBounds(35, 240, 150, 50)
        panel.add(btnLeaderboards)
        btnPreferences.font = buttonFont
        btnPreferences.setBounds(505, 40, 150, 50)
        panel.add(btnPreferences)
        btnDartzeeTemplates.font = buttonFont
        btnDartzeeTemplates.setBounds(590, 140, 150, 50)
        panel.add(btnDartzeeTemplates)
        btnAbout.font = buttonFont
        btnAbout.setBounds(615, 240, 150, 50)
        panel.add(btnAbout)
        btnUtilities.font = buttonFont
        btnUtilities.setBounds(590, 340, 150, 50)
        panel.add(btnUtilities)
        btnExit.font = buttonFont
        btnExit.setBounds(325, 465, 150, 50)
        panel.add(btnExit)
        btnGameReport.font = buttonFont
        btnGameReport.setBounds(60, 340, 150, 50)
        panel.add(btnGameReport)

        menuDartboard.setBounds(200, 65, 400, 400)
        menuDartboard.paintDartboard(null, false)
        panel.add(menuDartboard)

        //Add ActionListeners
        addActionListenerToAllChildren(this)
    }

    override fun getScreenName() = "Menu"

    override fun initialise()
    {
        //Do nothing
    }

    override fun showBackButton() = false

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnAbout -> {
                val dialog = AboutDialog()
                dialog.setLocationRelativeTo(this)
                dialog.isModal = true
                dialog.isVisible = true
            }

            btnPreferences -> ScreenCache.switch<PreferencesScreen>()
            btnExit -> exitApplication()
            btnNewGame -> ScreenCache.switch<GameSetupScreen>()
            btnManagePlayers -> ScreenCache.switch<PlayerManagementScreen>()
            btnGameReport -> ScreenCache.switch<ReportingSetupScreen>()
            btnLeaderboards -> ScreenCache.switch<LeaderboardsScreen>()
            btnUtilities -> ScreenCache.switch<UtilitiesScreen>()
            btnDartzeeTemplates -> ScreenCache.switch<DartzeeTemplateSetupScreen>()
            else -> super.actionPerformed(arg0)
        }
    }
}
