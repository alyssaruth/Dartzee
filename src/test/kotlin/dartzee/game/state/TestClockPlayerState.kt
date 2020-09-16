package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.game.ClockType
import dartzee.helper.AbstractTest
import dartzee.helper.makeClockPlayerStateWithRounds
import dartzee.helper.makeDart
import io.kotlintest.shouldBe
import org.junit.Test

class TestClockPlayerState: AbstractTest()
{
    @Test
    fun `should correctly compute the current target based on clockType`()
    {
        val roundOne = listOf(makeDart(1, 2, startingScore = 1))
        val state = makeClockPlayerStateWithRounds(dartsThrown = listOf(roundOne))

        state.getCurrentTarget(ClockType.Standard) shouldBe 2
        state.getCurrentTarget(ClockType.Doubles) shouldBe 2
        state.getCurrentTarget(ClockType.Trebles) shouldBe 1
    }

    @Test
    fun `Should report a target of one when no darts thrown`()
    {
        val state = makeClockPlayerStateWithRounds()
        state.getCurrentTarget(ClockType.Standard) shouldBe 1
    }

    @Test
    fun `Should combine all thrown darts to calculate current target`()
    {
        val roundOne = listOf(
            makeDart(1, 1, startingScore = 1),
            makeDart(2, 0, startingScore = 2),
            makeDart(2, 3, startingScore = 2))

        val state = makeClockPlayerStateWithRounds(dartsThrown = listOf(roundOne))
        state.getCurrentTarget(ClockType.Standard) shouldBe 3

        state.dartThrown(makeDart(3, 1, startingScore = 3))
        state.getCurrentTarget(ClockType.Standard) shouldBe 4

        state.resetRound()
        state.getCurrentTarget(ClockType.Standard) shouldBe 3
    }

    @Test
    fun `Should report a score of 0 when no darts thrown`()
    {
        val state = makeClockPlayerStateWithRounds()
        state.getScoreSoFar() shouldBe 0
    }

    @Test
    fun `Should report a score based on how many darts have been thrown, including uncommitted ones`()
    {
        val roundOne = listOf(Dart(1, 1), Dart(2, 1), Dart(3, 1), Dart(4, 1))
        val roundTwo = listOf(Dart(5, 0), Dart(5, 0), Dart(5, 0))

        val state = makeClockPlayerStateWithRounds(dartsThrown = listOf(roundOne, roundTwo))
        state.getScoreSoFar() shouldBe 7

        state.dartThrown(Dart(5, 1))
        state.getScoreSoFar() shouldBe 8

        state.resetRound()
        state.getScoreSoFar() shouldBe 7
    }

}