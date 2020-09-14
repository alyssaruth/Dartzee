package dartzee.screen.game.golf

import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.game.state.GolfPlayerState
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsMatchScreen
import dartzee.screen.game.MatchSummaryPanel

class GolfMatchScreen(match: DartsMatchEntity, players: List<PlayerEntity>):
    DartsMatchScreen<GolfPlayerState>(MatchSummaryPanel(match, MatchStatisticsPanelGolf()), match, players)
{
    override fun factoryGamePanel(parent: AbstractDartsGameScreen, game: GameEntity) = GamePanelGolf(parent, game, match.getPlayerCount())
}