package dartzee.screen.dartzee

import dartzee.db.DartsMatchEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.game.GameLaunchParams
import dartzee.screen.EmbeddedScreen
import dartzee.screen.GameSetupScreen
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.gameLauncher
import java.awt.BorderLayout

class DartzeeRuleSetupScreen : EmbeddedScreen()
{
    private val dartzeeRulePanel = DartzeeRuleSetupPanel()

    var match: DartsMatchEntity? = null
    var players: List<PlayerEntity> = listOf()
    var pairMode: Boolean = false

    init
    {
        add(dartzeeRulePanel, BorderLayout.CENTER)
    }

    override fun initialise() {}

    fun setState(match: DartsMatchEntity?, players: List<PlayerEntity>, pairMode: Boolean)
    {
        this.match = match
        this.players = players
        this.pairMode = pairMode

        btnNext.text = getNextText() + " >"
    }

    override fun nextPressed()
    {
        val dtos = dartzeeRulePanel.getRules()
        val launchParams = GameLaunchParams(players, GameType.DARTZEE, "", pairMode, dtos)

        if (match != null)
        {
            gameLauncher.launchNewMatch(match!!, launchParams)
        }
        else
        {
            gameLauncher.launchNewGame(launchParams)
        }
    }

    override fun getScreenName() = "Dartzee - Custom Setup"
    override fun getBackTarget() = ScreenCache.get<GameSetupScreen>()
    override fun showNextButton() = true
    override fun getNextText() = if (match != null) "Launch Match" else "Launch Game"
}