package dartzee.screen.dartzee

import dartzee.db.DartsMatchEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.EmbeddedScreen
import dartzee.screen.GameSetupScreen
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.gameLauncher
import java.awt.BorderLayout

class DartzeeRuleSetupScreen : EmbeddedScreen()
{
    private val dartzeeRulePanel = DartzeeRuleSetupPanel()

    private var match: DartsMatchEntity? = null
    private var players: List<PlayerEntity> = listOf()

    init
    {
        add(dartzeeRulePanel, BorderLayout.CENTER)
    }

    override fun initialise() {}

    fun setState(match: DartsMatchEntity?, players: List<PlayerEntity>)
    {
        this.match = match
        this.players = players

        btnNext.text = getNextText() + " >"
    }

    override fun nextPressed()
    {
        val dtos = dartzeeRulePanel.getRules()

        if (match != null)
        {
            gameLauncher.launchNewMatch(match!!, dtos)
        }
        else
        {
            gameLauncher.launchNewGame(players, GameType.DARTZEE, "", dtos)
        }
    }

    override fun getScreenName() = "Dartzee - Custom Setup"
    override fun getBackTarget() = ScreenCache.getScreen(GameSetupScreen::class.java)
    override fun showNextButton() = true
    override fun getNextText() = if (match != null) "Launch Match" else "Launch Game"
}