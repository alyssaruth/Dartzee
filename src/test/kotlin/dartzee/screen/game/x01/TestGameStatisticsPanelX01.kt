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
        val panel = GameStatisticsPanelX01("501")
        panel.nfSetupThreshold.getMaximum() shouldBe 500

        val panelTwo = GameStatisticsPanelX01("301")
        panelTwo.nfSetupThreshold.getMaximum() shouldBe 300
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

    @Test
    fun `Should populate the score breakdown rows correctly, and respond to setup threshold changing`()
    {
        val roundOne = listOf(Dart(20, 1), Dart(1, 1), Dart(5, 1)) //26 - 475
        val roundTwo = listOf(Dart(1, 1), Dart(18, 3), Dart(20, 1)) //75 - 400
        val roundThree = listOf(Dart(12, 1), Dart(19, 1), Dart(9, 1)) //40 - 360
        val roundFour = listOf(Dart(1, 1), Dart(5, 1), Dart(1, 1)) //7 - 353
        val roundFive = listOf(Dart(20, 1), Dart(1, 1), Dart(1, 1)) //22 - 331
        val roundSix = listOf(Dart(20, 3), Dart(20, 1), Dart(20, 1)) //100 - 231

        //Sort out the startingScores
        makeX01Rounds(501, roundOne, roundTwo, roundThree, roundFour, roundFive, roundSix)

        val state = makeDefaultPlayerState<DartsScorerX01>(insertPlayer(name = "Alice"), dartsThrown = roundOne)
        val statsPanel = GameStatisticsPanelX01("501")
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("20 - 39" to 1))

        state.addDarts(roundTwo)
        state.addDarts(roundThree)
        state.addDarts(roundFour)

        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("20 - 39" to 1, "60 - 79" to 1, "40 - 59" to 1, "0 - 19" to 1))

        state.addDarts(roundFive)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("20 - 39" to 2, "60 - 79" to 1, "40 - 59" to 1, "0 - 19" to 1))

        state.addDarts(roundSix)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("20 - 39" to 2, "60 - 79" to 1, "40 - 59" to 1, "0 - 19" to 1, "100 - 139" to 1))

        statsPanel.nfSetupThreshold.value = 330
        statsPanel.shouldHaveBreakdownState(hashMapOf("20 - 39" to 2, "60 - 79" to 1, "40 - 59" to 1, "0 - 19" to 1, "100 - 139" to 0))
    }

    @Test
    fun `Should populate the score breakdown rows correctly - remaining cases`()
    {
        val roundOne = listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)) //180 - 521
        val roundTwo = listOf(Dart(20, 3), Dart(20, 3), Dart(19, 3)) //177 - 344
        val roundThree = listOf(Dart(20, 3), Dart(20, 3), Dart(20, 1)) //140 - 204
        val roundFour = listOf(Dart(20, 3), Dart(20, 1), Dart(20, 0)) //80 - 124

        //Sort out the startingScores
        makeX01Rounds(701, roundOne, roundTwo, roundThree, roundFour)

        val state = makeDefaultPlayerState<DartsScorerX01>(insertPlayer(name = "Alice"), dartsThrown = roundOne)
        val statsPanel = GameStatisticsPanelX01("701")
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("180" to 1))

        state.addDarts(roundTwo)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("140 - 179" to 1, "180" to 1))

        state.addDarts(roundThree)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("140 - 179" to 2, "180" to 1))

        state.addDarts(roundFour)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("140 - 179" to 2, "180" to 1, "80 - 99" to 1))
    }

    private fun GameStatisticsPanelX01.shouldHaveBreakdownState(map: Map<String, Int>)
    {
        val rows = listOf("180", "140 - 179", "100 - 139", "80 - 99", "60 - 79", "40 - 59", "20 - 39", "0 - 19")

        rows.forEach {
            getValueForRow(it) shouldBe (map[it] ?: 0)
        }
    }


}