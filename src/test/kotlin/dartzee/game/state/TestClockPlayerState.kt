package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.game.ClockType
import dartzee.helper.AbstractTest
import dartzee.helper.makeClockPlayerState
import dartzee.helper.makeDart
import io.kotlintest.shouldBe
import org.junit.Test

class TestClockPlayerState: AbstractTest()
{
    @Test
    fun `should correctly compute the current target based on clockType`()
    {
        val roundOne = listOf(makeDart(1, 2, startingScore = 1))

        makeClockPlayerState(ClockType.Standard, completedRounds = listOf(roundOne)).getCurrentTarget() shouldBe 2
        makeClockPlayerState(ClockType.Doubles, completedRounds = listOf(roundOne)).getCurrentTarget() shouldBe 2
        makeClockPlayerState(ClockType.Trebles, completedRounds = listOf(roundOne)).getCurrentTarget() shouldBe 1
    }

    @Test
    fun `Should report a target of one when no darts thrown`()
    {
        val state = makeClockPlayerState()
        state.getCurrentTarget() shouldBe 1
    }

    @Test
    fun `Should combine all thrown darts to calculate current target`()
    {
        val roundOne = listOf(
            makeDart(1, 1, startingScore = 1),
            makeDart(2, 0, startingScore = 2),
            makeDart(2, 3, startingScore = 2))

        val state = makeClockPlayerState(completedRounds = listOf(roundOne))
        state.getCurrentTarget() shouldBe 3

        state.dartThrown(makeDart(3, 1, startingScore = 3))
        state.getCurrentTarget() shouldBe 4

        state.resetRound()
        state.getCurrentTarget() shouldBe 3
    }

    @Test
    fun `Should report a score of 0 when no darts thrown`()
    {
        val state = makeClockPlayerState()
        state.getScoreSoFar() shouldBe 0
    }

    @Test
    fun `Should report a score based on how many darts have been thrown, including uncommitted ones`()
    {
        val roundOne = listOf(Dart(1, 1), Dart(2, 1), Dart(3, 1), Dart(4, 1))
        val roundTwo = listOf(Dart(5, 0), Dart(5, 0), Dart(5, 0))

        val state = makeClockPlayerState(completedRounds = listOf(roundOne, roundTwo))
        state.getScoreSoFar() shouldBe 7

        state.dartThrown(Dart(5, 1))
        state.getScoreSoFar() shouldBe 8

        state.resetRound()
        state.getScoreSoFar() shouldBe 7
    }

    @Test
    fun `Should update darts that are thrown with their starting score`()
    {
        val roundOne = listOf(
            makeDart(1, 1, startingScore = 1),
            makeDart(2, 0, startingScore = 2),
            makeDart(2, 3, startingScore = 2))

        val state = makeClockPlayerState(completedRounds = listOf(roundOne))
        state.getCurrentTarget() shouldBe 3

        val dart = Dart(3, 1)
        val dartTwo = Dart(4, 0)
        state.dartThrown(dart)
        state.dartThrown(dartTwo)
        dart.startingScore shouldBe 3
        dartTwo.startingScore shouldBe 4
    }

}