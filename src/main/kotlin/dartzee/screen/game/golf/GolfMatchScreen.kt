package dartzee.screen.game.golf

import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.game.state.DefaultPlayerState
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsMatchScreen
import dartzee.screen.game.MatchSummaryPanel

class GolfMatchScreen(match: DartsMatchEntity, players: List<PlayerEntity>):
    DartsMatchScreen<DefaultPlayerState>(MatchSummaryPanel(match, MatchStatisticsPanelGolf()), match, players)
{
    override fun factoryGamePanel(parent: AbstractDartsGameScreen, game: GameEntity) = GamePanelGolf(parent, game, match.getPlayerCount())
}