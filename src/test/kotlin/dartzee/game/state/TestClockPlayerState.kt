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

    @Test
    fun `Should correctly count the longest streak, taking into account clockType`()
    {
        val standardState = makeClockPlayerState(clockType = ClockType.Standard)
        standardState.getLongestStreak() shouldBe 0
        standardState.dartThrown(Dart(1, 1))
        standardState.dartThrown(Dart(2, 1))
        standardState.dartThrown(Dart(3, 0))
        standardState.getLongestStreak() shouldBe 2

        val doublesState = makeClockPlayerState(clockType = ClockType.Doubles)
        doublesState.dartThrown(Dart(1, 2))
        doublesState.dartThrown(Dart(2, 1))
        doublesState.getLongestStreak() shouldBe 1
    }

    @Test
    fun `Should correctly report on track for Brucey throughout a successful round`()
    {
        val state = makeClockPlayerState()
        state.onTrackForBrucey() shouldBe true

        state.dartThrown(Dart(1, 1))
        state.onTrackForBrucey() shouldBe true

        state.dartThrown(Dart(2, 2))
        state.onTrackForBrucey() shouldBe true

        state.dartThrown(Dart(3, 1))
        state.onTrackForBrucey() shouldBe true

        state.dartThrown(Dart(4, 3))
        state.onTrackForBrucey() shouldBe true
    }
    
    @Test
    fun `Longest streak should take into account previous rounds`()
    {
        val roundOne = listOf(makeDart(1, 0, startingScore = 1), makeDart(1, 1, startingScore = 1), makeDart(2, 1, startingScore = 2))
        val state = makeClockPlayerState(completedRounds = listOf(roundOne))
        state.getLongestStreak() shouldBe 2

        state.dartThrown(Dart(3, 1))
        state.getLongestStreak() shouldBe 3

        state.resetRound()
        state.getLongestStreak() shouldBe 2
    }

    @Test
    fun `Should report not on track for Brucey as soon as there has been a miss, taking into account clockType`()
    {
        val state = makeClockPlayerState(clockType = ClockType.Doubles)

        state.dartThrown(Dart(1, 1))
        state.onTrackForBrucey() shouldBe false

        state.resetRound()
        state.dartThrown(Dart(1, 2))
        state.onTrackForBrucey() shouldBe true

        state.dartThrown(Dart(2, 3))
        state.onTrackForBrucey() shouldBe false
    }

}