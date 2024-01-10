package dartzee.screen.game.golf

import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.game.state.GolfPlayerState
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsMatchScreen
import dartzee.screen.game.MatchSummaryPanel

class GolfMatchScreen(match: DartsMatchEntity) :
    DartsMatchScreen<GolfPlayerState>(MatchSummaryPanel(match, MatchStatisticsPanelGolf()), match) {
    override fun factoryGamePanel(
        parent: AbstractDartsGameScreen,
        game: GameEntity,
        totalPlayers: Int
    ) = GamePanelGolf(parent, game, totalPlayers)
}
