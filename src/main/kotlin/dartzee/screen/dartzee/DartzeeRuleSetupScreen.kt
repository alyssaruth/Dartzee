package dartzee.screen.dartzee

import dartzee.db.DartsMatchEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameLaunchParams
import dartzee.game.GameType
import dartzee.screen.EmbeddedScreen
import dartzee.screen.GameSetupScreen
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.gameLauncher
import java.awt.BorderLayout

class DartzeeRuleSetupScreen(
    private val match: DartsMatchEntity?,
    private val players: List<PlayerEntity>,
    private val pairMode: Boolean
) : EmbeddedScreen() {
    private val dartzeeRulePanel = DartzeeRuleSetupPanel()

    init {
        add(dartzeeRulePanel, BorderLayout.CENTER)
    }

    override fun initialise() {}

    override fun nextPressed() {
        val dtos = dartzeeRulePanel.getRules()
        val launchParams = GameLaunchParams(players, GameType.DARTZEE, "", pairMode, dtos)

        if (match != null) {
            gameLauncher.launchNewMatch(match, launchParams)
        } else {
            gameLauncher.launchNewGame(launchParams)
        }
    }

    override fun getScreenName() = "Dartzee - Custom Setup"

    override fun getBackTarget() = ScreenCache.get<GameSetupScreen>()

    override fun showNextButton() = true

    override fun getNextText() = if (match != null) "Launch Match" else "Launch Game"
}
