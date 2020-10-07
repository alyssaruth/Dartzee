package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.game.ClockType
import dartzee.helper.AbstractTest
import dartzee.helper.makeClockPlayerState
import dartzee.helper.makeDart
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class TestClockPlayerState: AbstractTest()
{
    @Test
    fun `should correctly compute the current target based on clockType`()
    {
        val roundOne = listOf(makeDart(1, 2, startingScore = 1))

        makeClockPlayerState(ClockType.Standard, completedRounds = listOf(roundOne)).findCurrentTarget() shouldBe 2
        makeClockPlayerState(ClockType.Doubles, completedRounds = listOf(roundOne)).findCurrentTarget() shouldBe 2
        makeClockPlayerState(ClockType.Trebles, completedRounds = listOf(roundOne)).findCurrentTarget() shouldBe 1
    }

    @Test
    fun `Should report a target of one when no darts thrown`()
    {
        val state = makeClockPlayerState()
        state.findCurrentTarget() shouldBe 1
    }

    @Test
    fun `Should combine all thrown darts to calculate current target`()
    {
        val roundOne = listOf(
            makeDart(1, 1, startingScore = 1),
            makeDart(2, 0, startingScore = 2),
            makeDart(2, 3, startingScore = 2))

        val state = makeClockPlayerState(completedRounds = listOf(roundOne))
        state.findCurrentTarget() shouldBe 3

        state.dartThrown(makeDart(3, 1, startingScore = 3))
        state.findCurrentTarget() shouldBe 4

        state.resetRound()
        state.findCurrentTarget() shouldBe 3
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
        state.findCurrentTarget() shouldBe 3

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

    @Test
    fun `Should report a current target of null for a finished player, and throw an exception if another dart is thrown`()
    {
        val rounds = (1..20).map { makeDart(it, 1, startingScore = it) }.chunked(4)
        val state = makeClockPlayerState(completedRounds = rounds)
        state.findCurrentTarget() shouldBe null

        shouldThrow<Exception> {
            state.dartThrown(Dart(1, 1))
        }

        shouldThrow<Exception> {
            state.getCurrentTarget()
        }
    }

    @Test
    fun `For out of order games, it should populate thrown darts with all remaining targets`()
    {
        val state = makeClockPlayerState(inOrder = false)

        val dartOne = Dart(5, 1)
        state.dartThrown(dartOne)
        dartOne.clockTargets shouldBe (1..20).toList()

        val dartTwo = Dart(1, 1)
        state.dartThrown(dartTwo)
        dartTwo.clockTargets shouldBe (1..20).filterNot { it == 5 }.toList()
    }

    @Test
    fun `For in order games, it should not populate thrown darts with all remaining targets`()
    {
        val state = makeClockPlayerState(inOrder = true)

        val dartOne = Dart(5, 1)
        state.dartThrown(dartOne)
        dartOne.clockTargets shouldBe emptyList()

        val dartTwo = Dart(1, 1)
        state.dartThrown(dartTwo)
        dartTwo.clockTargets shouldBe emptyList()
    }

    @Test
    fun `Should correctly report out of order targets as hit`()
    {
        val state = makeClockPlayerState(inOrder = false)
        state.dartThrown(Dart(5, 1))
        state.hasHitTarget(5) shouldBe true
    }

    @Test
    fun `Should not report out of order targets if in ordered mode`()
    {
        val state = makeClockPlayerState(inOrder = true)

        val dartOne = Dart(5, 1)
        state.dartThrown(dartOne)
        state.hasHitTarget(5) shouldBe false

        state.dartThrown(Dart(1, 1))
        state.hasHitTarget(1) shouldBe true
    }

    @Test
    fun `Should report the correct current target in out of order mode`()
    {
        val state = makeClockPlayerState(inOrder = false)

        state.dartThrown(Dart(2, 1))
        state.dartThrown(Dart(4, 1))
        state.dartThrown(Dart(6, 1))
        state.commitRound()

        state.getCurrentTarget() shouldBe 1

        state.dartThrown(Dart(1, 1))
        state.getCurrentTarget() shouldBe 3

        state.dartThrown(Dart(3, 1))
        state.getCurrentTarget() shouldBe 5
    }

    @Test
    fun `Should only report on track for brucey if hit in the right order`()
    {
        val state = makeClockPlayerState(inOrder = false)

        state.dartThrown(Dart(2, 1))
        state.dartThrown(Dart(5, 1))
        state.dartThrown(Dart(20, 1))
        state.commitRound()

        state.dartThrown(Dart(6, 1))
        state.onTrackForBrucey() shouldBe false

        state.resetRound()
        state.dartThrown(Dart(1, 1))
        state.onTrackForBrucey() shouldBe true

        state.dartThrown(Dart(3, 1))
        state.onTrackForBrucey() shouldBe true

        state.dartThrown(Dart(4, 1))
        state.onTrackForBrucey() shouldBe true
    }
}