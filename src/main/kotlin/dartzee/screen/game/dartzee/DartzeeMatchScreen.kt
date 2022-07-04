package dartzee.screen.game.dartzee

import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.game.state.DartzeePlayerState
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsGamePanel.Companion.constructGamePanelDartzee
import dartzee.screen.game.DartsMatchScreen
import dartzee.screen.game.MatchSummaryPanel

class DartzeeMatchScreen(match: DartsMatchEntity):
    DartsMatchScreen<DartzeePlayerState>(MatchSummaryPanel(match, MatchStatisticsPanelDartzee()), match)
{
    override fun factoryGamePanel(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int) =
        constructGamePanelDartzee(parent, game, totalPlayers)
}