package dartzee.screen

import dartzee.bean.PresentationDartboard
import dartzee.core.bean.LinkLabel
import dartzee.core.util.addActionListenerToAllChildren
import dartzee.screen.dartzee.DartzeeTemplateSetupScreen
import dartzee.screen.player.PlayerManagementScreen
import dartzee.screen.preference.PreferencesScreen
import dartzee.screen.reporting.ReportingSetupScreen
import dartzee.screen.stats.overall.LeaderboardsScreen
import dartzee.screen.sync.SyncManagementScreen
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.ResourceCache
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridLayout
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class MenuScreen : EmbeddedScreen()
{
    private val dartboard = PresentationDartboard()
    private val btnNewGame = JButton("New Game")
    private val btnManagePlayers = JButton("Manage Players")
    private val btnLeaderboards = JButton("Leaderboards")
    private val btnPreferences = JButton("Preferences")
    private val btnDartzeeTemplates = JButton("Dartzee")
    private val btnUtilities = JButton("Utilities")
    private val btnSyncSummary = JButton("Sync Setup")
    private val btnGameReport = JButton("Game Report")
    private val lblVersion = LinkLabel("Dartzee $DARTS_VERSION_NUMBER", ::linkClicked)

    private val buttonFont = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 18f)

    init
    {
        add(dartboard, BorderLayout.CENTER)

        val panelEast = JPanel()
        val panelWest = JPanel()

        add(panelEast, BorderLayout.EAST)
        add(panelWest, BorderLayout.WEST)

        panelEast.layout = GridLayout(4, 1, 0, 20)
        panelWest.layout = GridLayout(4, 1, 0, 20)
        panelEast.border = EmptyBorder(20, 0, 20, 15)
        panelWest.border = EmptyBorder(20, 15, 20, 0)

        panelEast.preferredSize = Dimension(200, 0)
        panelWest.preferredSize = Dimension(200, 0)

        btnNewGame.font = buttonFont
        btnManagePlayers.font = buttonFont
        btnLeaderboards.font = buttonFont
        btnGameReport.font = buttonFont
        panelWest.add(btnNewGame)
        panelWest.add(btnManagePlayers)
        panelWest.add(btnLeaderboards)
        panelWest.add(btnGameReport)

        btnPreferences.font = buttonFont
        panelEast.add(btnPreferences)
        btnDartzeeTemplates.font = buttonFont
        panelEast.add(btnDartzeeTemplates)
        btnUtilities.font = buttonFont
        panelEast.add(btnUtilities)
        btnSyncSummary.font = buttonFont
        panelEast.add(btnSyncSummary)

        val southLayout = FlowLayout()
        southLayout.alignment = FlowLayout.TRAILING
        val panelSouth = JPanel()
        panelSouth.layout = southLayout
        add(panelSouth, BorderLayout.SOUTH)
        panelSouth.add(lblVersion)

        //Add ActionListeners
        addActionListenerToAllChildren(this)
    }

    override fun getScreenName() = "Menu"

    override fun initialise()
    {
        // Do nothing
    }

    private fun linkClicked()
    {
        ChangeLog().also {
            it.setLocationRelativeTo(this)
            it.isVisible = true
        }
    }

    override fun showBackButton() = false

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnPreferences -> ScreenCache.switch<PreferencesScreen>()
            btnSyncSummary -> ScreenCache.switch<SyncManagementScreen>()
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
