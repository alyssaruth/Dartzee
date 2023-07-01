package dartzee.screen

import dartzee.bean.PresentationDartboard
import dartzee.core.bean.LinkLabel
import dartzee.core.util.getAllChildComponentsForType
import dartzee.screen.dartzee.DartzeeTemplateSetupScreen
import dartzee.screen.player.PlayerManagementScreen
import dartzee.screen.preference.PreferencesScreen
import dartzee.screen.reporting.ReportingSetupScreen
import dartzee.screen.stats.overall.LeaderboardsScreen
import dartzee.screen.sync.SyncManagementScreen
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.ResourceCache
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
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
        val panelCenter = JPanel()
        panelCenter.layout = BorderLayout(0, 0)
        add(panelCenter, BorderLayout.CENTER)
        panelCenter.add(dartboard, BorderLayout.CENTER)

        val panelNorth = JPanel()
        val panelEast = JPanel()
        val panelWest = JPanel()
        val panelSouth = JPanel()

        val northLayout = FlowLayout()
        northLayout.hgap = 50
        panelNorth.layout = northLayout

        val southLayout = FlowLayout()
        southLayout.hgap = 50
        panelSouth.layout = southLayout

        panelCenter.add(panelNorth, BorderLayout.NORTH)
        panelCenter.add(panelEast, BorderLayout.EAST)
        panelCenter.add(panelWest, BorderLayout.WEST)
        panelCenter.add(panelSouth, BorderLayout.SOUTH)

        panelNorth.add(btnNewGame)
        panelNorth.add(btnPreferences)
        panelSouth.add(btnGameReport)
        panelSouth.add(btnSyncSummary)

        panelEast.layout = MigLayout("al center center, wrap, gapy 50")
        panelWest.layout = MigLayout("al center center, wrap, gapy 50")
        panelEast.border = EmptyBorder(0, 0, 0, 15)
        panelWest.border = EmptyBorder(0, 15, 0, 0)

        panelEast.preferredSize = Dimension(200, 0)
        panelWest.preferredSize = Dimension(200, 0)

        panelWest.add(btnManagePlayers)
        panelWest.add(btnLeaderboards)
        panelEast.add(btnDartzeeTemplates)
        panelEast.add(btnUtilities)

        val versionLayout = FlowLayout()
        versionLayout.alignment = FlowLayout.TRAILING
        val panelVersion = JPanel()
        panelVersion.border = EmptyBorder(0, 0, 10, 15)
        panelVersion.layout = versionLayout
        add(panelVersion, BorderLayout.SOUTH)
        panelVersion.add(lblVersion)

        //Add ActionListeners
        getAllChildComponentsForType<JButton>().forEach { button ->
            button.preferredSize = Dimension(200, 80)
            button.font = buttonFont
            button.addActionListener(this)
        }
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
