package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.helper.AbstractTest
import dartzee.helper.makeX01PlayerStateWithRounds
import io.kotlintest.shouldBe
import org.junit.Test

class TestX01PlayerState: AbstractTest()
{
    @Test
    fun `should report correct score if no darts thrown`()
    {
        val state = makeX01PlayerStateWithRounds(dartsThrown = listOf())
        state.getScoreSoFar() shouldBe 0
    }

    @Test
    fun `should count completed rounds as 3 darts, regardless of how many were actually thrown`()
    {
        val roundOne = listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1))
        val roundTwo = listOf(Dart(20, 3))
        val roundThree = listOf(Dart(20, 1), Dart(20, 2))

        val state = makeX01PlayerStateWithRounds(dartsThrown = listOf(roundOne, roundTwo, roundThree))
        state.getScoreSoFar() shouldBe 9
    }

    @Test
    fun `should add on darts from the in progress round`()
    {
        val roundOne = listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1))
        val roundTwo = listOf(Dart(20, 3))

        val state = makeX01PlayerStateWithRounds(dartsThrown = listOf(roundOne, roundTwo))
        state.getScoreSoFar() shouldBe 6

        state.dartThrown(Dart(20, 1))
        state.getScoreSoFar() shouldBe 7

        state.dartThrown(Dart(5, 1))
        state.getScoreSoFar() shouldBe 8

        state.resetRound()
        state.getScoreSoFar() shouldBe 6
    }
}