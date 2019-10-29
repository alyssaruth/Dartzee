package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.db.DartsMatchEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.GameSetupScreen
import burlton.dartzee.code.screen.ScreenCache
import java.awt.BorderLayout

class DartzeeRuleSetupScreen : EmbeddedScreen()
{
    private val dartzeeRulePanel = DartzeeRuleSetupPanel()

    init
    {
        add(dartzeeRulePanel, BorderLayout.CENTER)
    }

    override fun initialise() { }

    fun setState(match: DartsMatchEntity?, players: MutableList<PlayerEntity>)
    {

    }

    override fun getScreenName() = "Dartzee Setup"
    override fun getBackTarget() = ScreenCache.getScreen(GameSetupScreen::class.java)
}