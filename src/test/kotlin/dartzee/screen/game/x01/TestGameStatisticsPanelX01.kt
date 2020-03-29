package dartzee.screen.game.x01

import dartzee.`object`.Dart
import dartzee.game.state.DefaultPlayerState
import dartzee.helper.insertPlayer
import dartzee.helper.makeDefaultPlayerState
import dartzee.helper.makeDefaultPlayerStateWithRounds
import dartzee.helper.makeX01Rounds
import dartzee.screen.game.AbstractGameStatisticsPanelTest
import dartzee.screen.game.getValueForRow
import dartzee.screen.game.scorer.DartsScorerX01
import io.kotlintest.shouldBe
import org.junit.Test

class TestGameStatisticsPanelX01: AbstractGameStatisticsPanelTest<DefaultPlayerState<DartsScorerX01>, GameStatisticsPanelX01>()
{
    override fun factoryStatsPanel() = GameStatisticsPanelX01("501")
    override fun makePlayerState() = makeDefaultPlayerState<DartsScorerX01>(dartsThrown = listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)))

    @Test
    fun `Should set the maximum setupThreshold to be 1 less than the starting score`()
    {

    }

    @Test
    fun `Should get correct highest score, lowest score and 3 dart average`()
    {
        //Alive - 26, 100
        val aliceDarts = makeX01Rounds(501, Dart(20, 1), Dart(5, 1), Dart(1, 1),Dart(20, 3), Dart(20, 1), Dart(20, 1))

        //Bob - 19, 40
        val bobDarts = makeX01Rounds(501, Dart(19, 1), Dart(3, 0), Dart(19, 0), Dart(17, 2), Dart(3, 1), Dart(3, 1))

        val aliceState = makeDefaultPlayerStateWithRounds<DartsScorerX01>(insertPlayer(name = "Alice"), dartsThrown = aliceDarts)
        val bobState = makeDefaultPlayerStateWithRounds<DartsScorerX01>(insertPlayer(name = "Bob"), dartsThrown = bobDarts)

        val statsPanel = factoryStatsPanel()
        statsPanel.showStats(listOf(aliceState, bobState))

        statsPanel.getValueForRow("Highest Score", 1) shouldBe 100
        statsPanel.getValueForRow("3-dart avg", 1) shouldBe 63.0
        statsPanel.getValueForRow("Lowest Score", 1) shouldBe 26

        statsPanel.getValueForRow("Highest Score", 2) shouldBe 40
        statsPanel.getValueForRow("3-dart avg", 2) shouldBe 29.5
        statsPanel.getValueForRow("Lowest Score", 2) shouldBe 19
    }

    @Test
    fun `Should adjust stats based on the setup threshold`()
    {
        //100, 120
        val aliceDarts = makeX01Rounds(501, Dart(20, 3), Dart(20, 1), Dart(20, 1), Dart(20, 3), Dart(20, 3), Dart(1, 0))
        val aliceState = makeDefaultPlayerStateWithRounds<DartsScorerX01>(insertPlayer(name = "Alice"), dartsThrown = aliceDarts)

        val statsPanel = factoryStatsPanel()
        statsPanel.showStats(listOf(aliceState))

        statsPanel.getValueForRow("Highest Score", 1) shouldBe 120
        statsPanel.getValueForRow("3-dart avg", 1) shouldBe 110.0
        statsPanel.getValueForRow("Lowest Score", 1) shouldBe 100
        statsPanel.getValueForRow("Treble %", 1) shouldBe 50.0
        statsPanel.getValueForRow("Miss %", 1) shouldBe 16.7

        statsPanel.nfSetupThreshold.value = 420

        statsPanel.getValueForRow("Highest Score", 1) shouldBe 100
        statsPanel.getValueForRow("3-dart avg", 1) shouldBe 100.0
        statsPanel.getValueForRow("Lowest Score", 1) shouldBe 100
        statsPanel.getValueForRow("Treble %", 1) shouldBe 33.3
        statsPanel.getValueForRow("Miss %", 1) shouldBe 0.0
    }

}