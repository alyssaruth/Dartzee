package dartzee.screen

import dartzee.bean.PresentationDartboard
import dartzee.core.bean.LinkLabel
import dartzee.core.util.getAllChildComponentsForType
import dartzee.screen.dartzee.DartzeeTemplateSetupScreen
import dartzee.screen.player.PlayerManagementScreen
import dartzee.screen.preference.PreferencesScreen
import dartzee.screen.reporting.ReportingSetupScreen
import dartzee.screen.stats.overall.LeaderboardsScreen
import dartzee.screen.stats.overall.SimplifiedLeaderboardScreen
import dartzee.screen.sync.SyncManagementScreen
import dartzee.theme.getBaseFont
import dartzee.theme.themedIcon
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.InjectedThings
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

        getAllChildComponentsForType<JButton>().forEach { button ->
            button.size = Dimension(BUTTON_WIDTH, BUTTON_HEIGHT)
            button.addActionListener(this)
        }

        refreshButtons()

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

    override fun fireAppearancePreferencesChanged() {
        super.fireAppearancePreferencesChanged()

        refreshButtons()
    }

    private fun refreshButtons() {
        lblVersion.refresh()

        btnNewGame.icon = themedIcon("/buttons/newGame.png")
        btnManagePlayers.icon = themedIcon("/buttons/playerManagement.png")
        btnUtilities.icon = themedIcon("/buttons/utilities.png")
        btnPreferences.icon = ImageIcon(javaClass.getResource("/buttons/preferences.png"))
        btnGameReport.icon = themedIcon("/buttons/gameReport.png")
        btnSyncSummary.icon = ImageIcon(javaClass.getResource("/buttons/sync.png"))
        btnDartzeeTemplates.icon = ImageIcon(javaClass.getResource("/buttons/dartzeeTemplates.png"))
        btnLeaderboards.icon = ImageIcon(javaClass.getResource("/buttons/leaderboards.png"))

        getAllChildComponentsForType<JButton>().forEach { button ->
            button.font = getBaseFont().deriveFont(Font.PLAIN, 18f)
        }
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
        btnPreferences.isVisible = false
        btnDartzeeTemplates.isVisible = false
        btnUtilities.isVisible = false
        btnSyncSummary.isVisible = false

        val widthToSubtract = maxOf(0, (minOf(width, height) + (BUTTON_WIDTH) + 50) - width)
        val dartboardSize = minOf(width, height) - widthToSubtract

        val dartboardX = 50
        val dartboardY = (height - dartboardSize) / 2
        dartboard.setSize(dartboardSize, dartboardSize)
        dartboard.setLocation(dartboardX, dartboardY)

        setButtonLocation(dartboard, btnNewGame, height, false, 1)
        setButtonLocation(dartboard, btnLeaderboards, height, false, 2)
    }

    private fun layoutFullScreen(width: Int, height: Int) {
        val widthToSubtract = maxOf(0, (minOf(width, height) + (2 * BUTTON_WIDTH) + 50) - width)
        val dartboardSize = minOf(width, height) - widthToSubtract

        val dartboardX = (width - dartboardSize) / 2
        val dartboardY = (height - dartboardSize) / 2
        dartboard.setSize(dartboardSize, dartboardSize)
        dartboard.setLocation(dartboardX, dartboardY)

        // Left
        setButtonLocation(dartboard, btnNewGame, height, true, 0)
        setButtonLocation(dartboard, btnManagePlayers, height, true, 1)
        setButtonLocation(dartboard, btnLeaderboards, height, true, 2)
        setButtonLocation(dartboard, btnGameReport, height, true, 3)

        // Right
        setButtonLocation(dartboard, btnPreferences, height, false, 0)
        setButtonLocation(dartboard, btnDartzeeTemplates, height, false, 1)
        setButtonLocation(dartboard, btnUtilities, height, false, 2)
        setButtonLocation(dartboard, btnSyncSummary, height, false, 3)

        lblVersion.setLocation(width - lblVersion.width - 5, height - lblVersion.height - 5)
    }

    private fun setButtonLocation(
        dartboard: PresentationDartboard,
        button: JButton,
        screenHeight: Int,
        left: Boolean,
        index: Int,
    ) {
        val yGapSpace = (screenHeight - (4 * BUTTON_HEIGHT))

        val btnYGap = maxOf(yGapSpace / 4, 40)
        val dartboardCenter = dartboard.y + (dartboard.height / 2)

        val y = dartboardCenter + ((index - 1.5) * btnYGap).toInt() + ((index - 2) * BUTTON_HEIGHT)
        if (left) {
            val x = if (index == 0 || index == 3) dartboard.x - 140 else dartboard.x - BUTTON_WIDTH
            button.setLocation(x, y)
        } else {
            val x =
                if (index == 0 || index == 3) dartboard.x + dartboard.width + 140 - BUTTON_WIDTH
                else dartboard.x + dartboard.width
            button.setLocation(x, y)
        }
    }

    private fun linkClicked() {
        changeLog.run {
            setLocationRelativeTo(this)
            isVisible = true
        }
    }

    private fun newGame() {
        if (InjectedThings.partyMode) {
            ScreenCache.switch<SimplePlayerSelectionScreen>()
        } else {
            ScreenCache.switch<GameSetupScreen>()
        }
    }

    private fun openLeaderboards() {
        if (InjectedThings.partyMode) {
            ScreenCache.switch<SimplifiedLeaderboardScreen>()
        } else {
            ScreenCache.switch<LeaderboardsScreen>()
        }
    }

    override fun showBackButton() = false

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source) {
            btnPreferences -> ScreenCache.switch<PreferencesScreen>()
            btnSyncSummary -> ScreenCache.switch<SyncManagementScreen>()
            btnNewGame -> newGame()
            btnManagePlayers -> ScreenCache.switch<PlayerManagementScreen>()
            btnGameReport -> ScreenCache.switch<ReportingSetupScreen>()
            btnLeaderboards -> openLeaderboards()
            btnUtilities -> ScreenCache.switch<UtilitiesScreen>()
            btnDartzeeTemplates -> ScreenCache.switch<DartzeeTemplateSetupScreen>()
            else -> super.actionPerformed(arg0)
        }
    }
}
