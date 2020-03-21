package dartzee.screen.game.dartzee

import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.game.state.DartzeePlayerState
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsGamePanel.Companion.constructGamePanelDartzee
import dartzee.screen.game.DartsMatchScreen
import dartzee.screen.game.MatchSummaryPanel

class DartzeeMatchScreen(match: DartsMatchEntity, players: List<PlayerEntity>):
    DartsMatchScreen<DartzeePlayerState>(MatchSummaryPanel(match, MatchStatisticsPanelDartzee(match.gameParams)), match, players)
{
    override fun factoryGamePanel(parent: AbstractDartsGameScreen, game: GameEntity) = constructGamePanelDartzee(parent, game)
}