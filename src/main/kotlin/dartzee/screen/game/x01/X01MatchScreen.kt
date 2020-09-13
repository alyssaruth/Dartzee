package dartzee.screen.game.x01

import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.game.state.DefaultPlayerState
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsMatchScreen
import dartzee.screen.game.MatchSummaryPanel

class X01MatchScreen(match: DartsMatchEntity, players: List<PlayerEntity>):
    DartsMatchScreen<DefaultPlayerState>(MatchSummaryPanel(match, MatchStatisticsPanelX01(match.gameParams)), match, players)
{
    override fun factoryGamePanel(parent: AbstractDartsGameScreen, game: GameEntity) = GamePanelX01(parent, game, match.getPlayerCount())
}