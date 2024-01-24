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
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.ImageIcon
import javax.swing.JButton

private const val BUTTON_WIDTH = 220
private const val BUTTON_HEIGHT = 80

class MenuScreen : EmbeddedScreen() {
    private val changeLog = ChangeLog()
    private val dartboard = PresentationDartboard()
    private val btnNewGame = JButton("New Game")
    private val btnManagePlayers = JButton("Manage Players")
    private val btnLeaderboards = JButton("Leaderboards")
    private val btnPreferences = JButton("Preferences")
    private val btnDartzeeTemplates = JButton("Dartzee Rules")
    private val btnUtilities = JButton("Utilities")
    private val btnSyncSummary = JButton("Sync Setup")
    private val btnGameReport = JButton("Game Report")
    private val lblVersion = LinkLabel("Dartzee $DARTS_VERSION_NUMBER", ::linkClicked)

    private val buttonFont = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 18f)

    init {
        layout = null
        layoutScreen(1000, 663)

        add(dartboard)

        add(btnNewGame)
        add(btnManagePlayers)
        add(btnLeaderboards)
        add(btnGameReport)

        add(btnPreferences)
        add(btnDartzeeTemplates)
        add(btnUtilities)
        add(btnSyncSummary)

        lblVersion.size = Dimension(100, 20)
        add(lblVersion)

        btnNewGame.icon = ImageIcon(javaClass.getResource("/buttons/newGame.png"))
        btnManagePlayers.icon = ImageIcon(javaClass.getResource("/buttons/playerManagement.png"))
        btnUtilities.icon = ImageIcon(javaClass.getResource("/buttons/utilities.png"))
        btnPreferences.icon = ImageIcon(javaClass.getResource("/buttons/preferences.png"))
        btnGameReport.icon = ImageIcon(javaClass.getResource("/buttons/gameReport.png"))
        btnSyncSummary.icon = ImageIcon(javaClass.getResource("/buttons/sync.png"))
        btnDartzeeTemplates.icon = ImageIcon(javaClass.getResource("/buttons/dartzeeTemplates.png"))
        btnLeaderboards.icon = ImageIcon(javaClass.getResource("/buttons/leaderboards.png"))

        getAllChildComponentsForType<JButton>().forEach { button ->
            button.size = Dimension(BUTTON_WIDTH, BUTTON_HEIGHT)
            button.font = buttonFont
            button.addActionListener(this)
        }

        addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(evt: ComponentEvent) = layoutScreen()
            }
        )
    }

    override fun getScreenName() = "Menu"

    override fun initialise() {
        // do nothing
    }

    override fun postInit() {
        super.postInit()
        layoutScreen()
    }

    private fun layoutScreen(width: Int = getWidth(), height: Int = getHeight()) {
        if (InjectedThings.partyMode) {
            layoutSimplifiedScreen(width, height)
        } else {
            layoutFullScreen(width, height)
        }
    }

    private fun layoutSimplifiedScreen(width: Int, height: Int) {
        btnManagePlayers.isVisible = false
        btnGameReport.isVisible = false
    }

    private fun layoutFullScreen(width: Int, height: Int) {
        val widthToSubtract = maxOf(0, (minOf(width, height) + (2 * BUTTON_WIDTH) + 50) - width)
        val dartboardSize = minOf(width, height) - widthToSubtract

        val dartboardX = (width - dartboardSize) / 2
        val dartboardY = (height - dartboardSize) / 2
        dartboard.setSize(dartboardSize, dartboardSize)
        dartboard.setLocation(dartboardX, dartboardY)

        val yGapSpace = (height - (4 * BUTTON_HEIGHT))

        val btnYGap = maxOf(yGapSpace / 4, 40)
        val dartboardCenter = dartboardY + (dartboardSize / 2)

        btnNewGame.setLocation(
            dartboardX - 140,
            dartboardCenter - (1.5 * btnYGap).toInt() - (2 * BUTTON_HEIGHT)
        )
        btnManagePlayers.setLocation(
            dartboardX - BUTTON_WIDTH,
            dartboardCenter - (0.5 * btnYGap).toInt() - BUTTON_HEIGHT
        )
        btnLeaderboards.setLocation(
            dartboardX - BUTTON_WIDTH,
            dartboardCenter + (0.5 * btnYGap).toInt()
        )
        btnGameReport.setLocation(
            dartboardX - 140,
            dartboardCenter + (1.5 * btnYGap).toInt() + BUTTON_HEIGHT
        )

        btnPreferences.setLocation(
            dartboardX + dartboardSize + 140 - BUTTON_WIDTH,
            dartboardCenter - (1.5 * btnYGap).toInt() - (2 * BUTTON_HEIGHT)
        )
        btnDartzeeTemplates.setLocation(
            dartboardX + dartboardSize,
            dartboardCenter - (0.5 * btnYGap).toInt() - BUTTON_HEIGHT
        )
        btnUtilities.setLocation(
            dartboardX + dartboardSize,
            dartboardCenter + (0.5 * btnYGap).toInt()
        )
        btnSyncSummary.setLocation(
            dartboardX + dartboardSize + 140 - BUTTON_WIDTH,
            dartboardCenter + (1.5 * btnYGap).toInt() + BUTTON_HEIGHT
        )

        lblVersion.setLocation(width - lblVersion.width - 5, height - lblVersion.height - 5)
    }

    private fun linkClicked() {
        changeLog.run {
            setLocationRelativeTo(this)
            isVisible = true
        }
    }

    override fun showBackButton() = false

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source) {
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
