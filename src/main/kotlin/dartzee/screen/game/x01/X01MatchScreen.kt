package dartzee.screen.game.x01

import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.game.state.IWrappedParticipant
import dartzee.game.state.X01PlayerState
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsMatchScreen
import dartzee.screen.game.MatchSummaryPanel

class X01MatchScreen(match: DartsMatchEntity, participants: List<IWrappedParticipant>):
    DartsMatchScreen<X01PlayerState>(MatchSummaryPanel(match, MatchStatisticsPanelX01(match.gameParams)), match, participants)
{
    override fun factoryGamePanel(parent: AbstractDartsGameScreen, game: GameEntity) = GamePanelX01(parent, game, match.getPlayerCount())
}