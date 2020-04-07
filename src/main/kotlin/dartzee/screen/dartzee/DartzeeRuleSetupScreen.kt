package dartzee.screen.dartzee

import dartzee.`object`.GameLauncher
import dartzee.db.DartsMatchEntity
import dartzee.db.GameType
import dartzee.db.PlayerEntity
import dartzee.screen.EmbeddedScreen
import dartzee.screen.GameSetupScreen
import dartzee.screen.ScreenCache
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
            GameLauncher.launchNewMatch(match!!, dtos)
        }
        else
        {
            GameLauncher.launchNewGame(players, GameType.DARTZEE, "", dtos)
        }
    }

    override fun getScreenName() = "Dartzee - Custom Setup"
    override fun getBackTarget() = ScreenCache.getScreen(GameSetupScreen::class.java)
    override fun showNextButton() = true
    override fun getNextText() = if (match != null) "Launch Match" else "Launch Game"
}