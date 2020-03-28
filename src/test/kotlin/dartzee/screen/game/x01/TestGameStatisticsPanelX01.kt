package dartzee.screen.game.x01

import dartzee.`object`.Dart
import dartzee.game.state.DefaultPlayerState
import dartzee.helper.makeDefaultPlayerState
import dartzee.screen.game.AbstractGameStatisticsPanelTest
import dartzee.screen.game.scorer.DartsScorerX01

class TestGameStatisticsPanelX01: AbstractGameStatisticsPanelTest<DefaultPlayerState<DartsScorerX01>, GameStatisticsPanelX01>()
{
    override fun factoryStatsPanel() = GameStatisticsPanelX01("501")
    override fun makePlayerState() = makeDefaultPlayerState<DartsScorerX01>(dartsThrown = listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)))

}