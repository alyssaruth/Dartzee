package dartzee.screen.game.x01

import com.github.alyssaburlton.swingtest.findChild
import dartzee.core.bean.NumberField
import dartzee.game.FinishType
import dartzee.game.X01Config
import dartzee.game.state.X01PlayerState
import dartzee.helper.insertPlayer
import dartzee.helper.makeX01PlayerState
import dartzee.helper.makeX01PlayerStateWithRounds
import dartzee.helper.makeX01Rounds
import dartzee.`object`.Dart
import dartzee.screen.game.AbstractGameStatisticsPanelTest
import dartzee.screen.game.getRowIndex
import dartzee.screen.game.getValueForRow
import dartzee.utils.InjectedThings
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestGameStatisticsPanelX01 :
    AbstractGameStatisticsPanelTest<X01PlayerState, GameStatisticsPanelX01>() {
    override fun factoryStatsPanel() = factoryStatsPanel(501)

    private fun factoryStatsPanel(startingScore: Int) =
        GameStatisticsPanelX01(X01Config(startingScore, FinishType.Doubles).toJson())

    override fun makePlayerState() =
        makeX01PlayerState(completedRound = listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)))

    @Test
    fun `Should set the maximum setupThreshold to be 1 less than the starting score`() {
        val panel = factoryStatsPanel(501)
        panel.nfSetupThreshold.getMaximum() shouldBe 500

        val panelTwo = factoryStatsPanel(301)
        panelTwo.nfSetupThreshold.getMaximum() shouldBe 300
    }

    @Test
    fun `Should not add setup threshold to screen in party mode`() {
        InjectedThings.partyMode = true

        val panel = factoryStatsPanel()
        panel.findChild<NumberField>().shouldBeNull()
    }

    @Test
    fun `Should get correct highest score, lowest score and 3 dart average`() {
        // Alive - 26, 100
        val aliceDarts =
            makeX01Rounds(
                501,
                Dart(20, 1),
                Dart(5, 1),
                Dart(1, 1),
                Dart(20, 3),
                Dart(20, 1),
                Dart(20, 1)
            )

        // Bob - 19, 40
        val bobDarts =
            makeX01Rounds(
                501,
                Dart(19, 1),
                Dart(3, 0),
                Dart(19, 0),
                Dart(17, 2),
                Dart(3, 1),
                Dart(3, 1)
            )

        val aliceState =
            makeX01PlayerStateWithRounds(
                player = insertPlayer(name = "Alice"),
                completedRounds = aliceDarts
            )
        val bobState =
            makeX01PlayerStateWithRounds(
                player = insertPlayer(name = "Bob"),
                completedRounds = bobDarts
            )

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
    fun `Should adjust stats based on the setup threshold`() {
        // 100, 120
        val aliceDarts =
            makeX01Rounds(
                501,
                Dart(20, 3),
                Dart(20, 1),
                Dart(20, 1),
                Dart(20, 3),
                Dart(20, 3),
                Dart(1, 0)
            )
        val aliceState =
            makeX01PlayerStateWithRounds(
                player = insertPlayer(name = "Alice"),
                completedRounds = aliceDarts
            )

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
    fun `Should populate the score breakdown rows correctly, and respond to setup threshold changing`() {
        val roundOne = listOf(Dart(20, 1), Dart(1, 1), Dart(5, 1)) // 26 - 475
        val roundTwo = listOf(Dart(1, 1), Dart(18, 3), Dart(20, 1)) // 75 - 400
        val roundThree = listOf(Dart(12, 1), Dart(19, 1), Dart(9, 1)) // 40 - 360
        val roundFour = listOf(Dart(1, 1), Dart(5, 1), Dart(1, 1)) // 7 - 353
        val roundFive = listOf(Dart(20, 1), Dart(1, 1), Dart(1, 1)) // 22 - 331
        val roundSix = listOf(Dart(20, 3), Dart(20, 1), Dart(20, 1)) // 100 - 231

        // Sort out the startingScores
        makeX01Rounds(501, roundOne, roundTwo, roundThree, roundFour, roundFive, roundSix)

        val state =
            makeX01PlayerState(player = insertPlayer(name = "Alice"), completedRound = roundOne)
        val statsPanel = factoryStatsPanel(501)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("20 - 39" to 1))

        state.addCompletedRound(roundTwo)
        state.addCompletedRound(roundThree)
        state.addCompletedRound(roundFour)

        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(
            hashMapOf("20 - 39" to 1, "60 - 79" to 1, "40 - 59" to 1, "0 - 19" to 1)
        )

        state.addCompletedRound(roundFive)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(
            hashMapOf("20 - 39" to 2, "60 - 79" to 1, "40 - 59" to 1, "0 - 19" to 1)
        )

        state.addCompletedRound(roundSix)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(
            hashMapOf(
                "20 - 39" to 2,
                "60 - 79" to 1,
                "40 - 59" to 1,
                "0 - 19" to 1,
                "100 - 139" to 1
            )
        )

        statsPanel.nfSetupThreshold.value = 330
        statsPanel.shouldHaveBreakdownState(
            hashMapOf(
                "20 - 39" to 2,
                "60 - 79" to 1,
                "40 - 59" to 1,
                "0 - 19" to 1,
                "100 - 139" to 0
            )
        )
    }

    @Test
    fun `Should populate the score breakdown rows correctly - remaining cases`() {
        val roundOne = listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)) // 180 - 521
        val roundTwo = listOf(Dart(20, 3), Dart(20, 3), Dart(19, 3)) // 177 - 344
        val roundThree = listOf(Dart(20, 3), Dart(20, 3), Dart(20, 1)) // 140 - 204
        val roundFour = listOf(Dart(20, 3), Dart(20, 1), Dart(20, 0)) // 80 - 124

        // Sort out the startingScores
        makeX01Rounds(701, roundOne, roundTwo, roundThree, roundFour)

        val state =
            makeX01PlayerState(player = insertPlayer(name = "Alice"), completedRound = roundOne)
        val statsPanel = factoryStatsPanel(701)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("180" to 1))

        state.addCompletedRound(roundTwo)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("140 - 179" to 1, "180" to 1))

        state.addCompletedRound(roundThree)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("140 - 179" to 2, "180" to 1))

        state.addCompletedRound(roundFour)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(hashMapOf("140 - 179" to 2, "180" to 1, "80 - 99" to 1))
    }

    @Test
    fun `Should populate top darts correctly, and respond to setup threshold changing`() {
        val roundOne = listOf(Dart(20, 1), Dart(20, 1), Dart(5, 1)) // 45 - 456
        val roundTwo = listOf(Dart(1, 1), Dart(18, 3), Dart(20, 1)) // 75 - 381
        val roundThree = listOf(Dart(12, 1), Dart(18, 1), Dart(1, 1)) // 40 - 350
        val roundFour = listOf(Dart(3, 1), Dart(5, 1), Dart(7, 1)) // 7 - 335
        val roundFive = listOf(Dart(19, 1), Dart(1, 1), Dart(1, 1)) // 21 - 314

        // Sort out the startingScores
        makeX01Rounds(501, roundOne, roundTwo, roundThree, roundFour, roundFive)

        val state =
            makeX01PlayerState(player = insertPlayer(name = "Alice"), completedRound = roundOne)
        val statsPanel = factoryStatsPanel(501)
        statsPanel.showStats(listOf(state))

        // [20, 20, 5]
        val sectionStart = statsPanel.getRowIndex("Top Darts")
        statsPanel.getValueForRow(sectionStart) shouldBe "20 [67%]"
        statsPanel.getValueForRow(sectionStart + 1) shouldBe "5 [33%]"
        statsPanel.getValueForRow(sectionStart + 2) shouldBe "N/A [0%]"

        // [20, 20, 20, 18, 5, 1]
        state.addCompletedRound(roundTwo)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow(sectionStart) shouldBe "20 [50%]"
        statsPanel.getValueForRow(sectionStart + 1) shouldBe "18 [17%]"
        statsPanel.getValueForRow(sectionStart + 2) shouldBe "5 [17%]"
        statsPanel.getValueForRow(sectionStart + 3) shouldBe "1 [17%]"
        statsPanel.getValueForRow(sectionStart + 4) shouldBe "N/A [0%]"

        // [20, 20, 20, 18, 18, 12, 5, 1, 1], still no remainder
        state.addCompletedRound(roundThree)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow(sectionStart) shouldBe "20 [33%]"
        statsPanel.getValueForRow(sectionStart + 1) shouldBe "18 [22%]"
        statsPanel.getValueForRow(sectionStart + 2) shouldBe "1 [22%]"
        statsPanel.getValueForRow(sectionStart + 3) shouldBe "12 [11%]"
        statsPanel.getValueForRow(sectionStart + 4) shouldBe "5 [11%]"
        statsPanel.getValueForRow(sectionStart + 5) shouldBe "0%"

        // [20, 20, 20, 18, 18, 5, 5, 1, 1, 12] [7, 3]
        state.addCompletedRound(roundFour)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow(sectionStart) shouldBe "20 [25%]"
        statsPanel.getValueForRow(sectionStart + 1) shouldBe "18 [17%]"
        statsPanel.getValueForRow(sectionStart + 2) shouldBe "5 [17%]"
        statsPanel.getValueForRow(sectionStart + 3) shouldBe "1 [17%]"
        statsPanel.getValueForRow(sectionStart + 4) shouldBe "12 [8%]"
        statsPanel.getValueForRow(sectionStart + 5) shouldBe "17%"

        // [1, 1, 1, 1, 20, 20, 20, 18, 18, 5, 5, 19] [12, 7, 3]
        state.addCompletedRound(roundFive)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow(sectionStart) shouldBe "1 [27%]"
        statsPanel.getValueForRow(sectionStart + 1) shouldBe "20 [20%]"
        statsPanel.getValueForRow(sectionStart + 2) shouldBe "18 [13%]"
        statsPanel.getValueForRow(sectionStart + 3) shouldBe "5 [13%]"
        statsPanel.getValueForRow(sectionStart + 4) shouldBe "19 [7%]"
        statsPanel.getValueForRow(sectionStart + 5) shouldBe "20%"

        // Knock out last round and part of the previous one
        // [20, 20, 20, 18, 18, 12, 5, 1, 1] [3]
        statsPanel.nfSetupThreshold.value = 348
        statsPanel.getValueForRow(sectionStart) shouldBe "20 [30%]"
        statsPanel.getValueForRow(sectionStart + 1) shouldBe "18 [20%]"
        statsPanel.getValueForRow(sectionStart + 2) shouldBe "1 [20%]"
        statsPanel.getValueForRow(sectionStart + 3) shouldBe "12 [10%]"
        statsPanel.getValueForRow(sectionStart + 4) shouldBe "5 [10%]"
        statsPanel.getValueForRow(sectionStart + 5) shouldBe "10%"
    }

    @Test
    fun `should correctly calculate checkout percentage`() {
        val roundOne = listOf(Dart(17, 1), Dart(20, 0), Dart(20, 0)) // On 40, two chances
        val roundTwo = listOf(Dart(5, 1), Dart(19, 1), Dart(8, 1)) // On 8, four chances
        val roundThree = listOf(Dart(4, 0), Dart(4, 2)) // Win, 6 chances = 16.7%

        val separateGameFinish = listOf(Dart(20, 0), Dart(20, 2)) // now 2 our of 8 changes = 25.0%

        makeX01Rounds(57, roundOne, roundTwo, roundThree)
        makeX01Rounds(40, separateGameFinish)

        val state = makeX01PlayerState(completedRound = roundOne)
        val statsPanel = factoryStatsPanel()

        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Checkout %") shouldBe "N/A" // not finished yet

        state.addCompletedRound(roundTwo)
        state.addCompletedRound(roundThree)

        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Checkout %") shouldBe 16.7

        state.addCompletedRound(separateGameFinish)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Checkout %") shouldBe 25.0
    }
}
