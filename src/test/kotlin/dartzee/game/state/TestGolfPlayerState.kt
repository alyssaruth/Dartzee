package dartzee.game.state

import dartzee.drtDoubleFour
import dartzee.drtDoubleOne
import dartzee.drtDoubleThree
import dartzee.drtInnerTwo
import dartzee.drtMissTwo
import dartzee.drtOuterOne
import dartzee.helper.AbstractTest
import dartzee.helper.makeDart
import dartzee.helper.makeGolfPlayerState
import dartzee.helper.makeGolfRound
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestGolfPlayerState : AbstractTest() {
    @Test
    fun `Should report a score of 0 when no darts thrown`() {
        val state = makeGolfPlayerState()
        state.getScoreSoFar() shouldBe 0
    }

    @Test
    fun `Should only take into account committed darts`() {
        val state = makeGolfPlayerState()
        state.dartThrown(makeDart(1, 1, golfHole = 1))
        state.dartThrown(makeDart(1, 2, golfHole = 1))
        state.getScoreSoFar() shouldBe 0

        state.commitRound()
        state.getScoreSoFar() shouldBe 1
    }

    @Test
    fun `Should sum the latest dart thrown from each round`() {
        val roundOne = makeGolfRound(1, listOf(makeDart(1, 1), makeDart(1, 3)))
        val roundTwo = makeGolfRound(2, listOf(makeDart(2, 0), makeDart(2, 1), makeDart(17, 1)))

        val state = makeGolfPlayerState(completedRounds = listOf(roundOne, roundTwo))
        state.getScoreSoFar() shouldBe 7
    }

    @Test
    fun `Should correctly report the score for each individual round`() {
        val roundOne = makeGolfRound(1, listOf(makeDart(1, 1), makeDart(1, 3)))
        val roundTwo = makeGolfRound(2, listOf(makeDart(2, 0), makeDart(2, 1), makeDart(17, 1)))

        val state = makeGolfPlayerState(completedRounds = listOf(roundOne, roundTwo))
        state.getScoreForRound(1) shouldBe 2
        state.getScoreForRound(2) shouldBe 5
    }

    @Test
    fun `Should calculate subtotals correctly`() {
        val roundOne = makeGolfRound(1, listOf(makeDart(1, 1), makeDart(1, 3)))
        val roundTwo = makeGolfRound(2, listOf(makeDart(2, 0), makeDart(2, 1), makeDart(17, 1)))
        val roundThree = makeGolfRound(3, listOf(makeDart(3, 2)))

        val state = makeGolfPlayerState(completedRounds = listOf(roundOne, roundTwo, roundThree))
        state.getCumulativeScoreForRound(1) shouldBe 2
        state.getCumulativeScoreForRound(2) shouldBe 7
        state.getCumulativeScoreForRound(3) shouldBe 8
    }

    @Test
    fun `Should count total hole in ones correctly`() {
        val roundOne = makeGolfRound(1, listOf(drtOuterOne(), drtDoubleOne()))
        val roundTwo = makeGolfRound(2, listOf(drtMissTwo(), drtInnerTwo()))
        val roundThree = makeGolfRound(3, listOf(drtDoubleThree()))

        val state = makeGolfPlayerState(completedRounds = listOf(roundOne, roundTwo, roundThree))
        state.countHoleInOnes() shouldBe 2

        state.dartThrown(drtDoubleFour())
        state.countHoleInOnes() shouldBe 3
    }
}
