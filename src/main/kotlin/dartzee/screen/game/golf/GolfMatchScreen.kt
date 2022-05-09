package dartzee.screen.game.golf

import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.game.state.GolfPlayerState
import dartzee.game.state.IWrappedParticipant
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsMatchScreen
import dartzee.screen.game.MatchSummaryPanel

class GolfMatchScreen(match: DartsMatchEntity, participants: List<IWrappedParticipant>):
    DartsMatchScreen<GolfPlayerState>(MatchSummaryPanel(match, MatchStatisticsPanelGolf()), match, participants)
{
    override fun factoryGamePanel(parent: AbstractDartsGameScreen, game: GameEntity) = GamePanelGolf(parent, game, match.getPlayerCount())
}