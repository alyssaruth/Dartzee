package dartzee.screen.game.rtc

import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.game.state.ClockPlayerState
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsMatchScreen
import dartzee.screen.game.MatchSummaryPanel

class RoundTheClockMatchScreen(match: DartsMatchEntity) :
    DartsMatchScreen<ClockPlayerState>(
        MatchSummaryPanel(match, MatchStatisticsPanelRoundTheClock(match.gameParams)),
        match
    ) {
    override fun factoryGamePanel(
        parent: AbstractDartsGameScreen,
        game: GameEntity,
        totalPlayers: Int
    ) = GamePanelRoundTheClock(parent, game, totalPlayers)
}
