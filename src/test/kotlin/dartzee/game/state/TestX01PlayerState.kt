package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.helper.AbstractTest
import dartzee.helper.makeDart
import dartzee.helper.makeX01PlayerStateWithRounds
import dartzee.helper.makeX01Rounds
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

    @Test
    fun `The remaining score should be the starting score if no darts have been thrown`()
    {
        val state = makeX01PlayerStateWithRounds(dartsThrown = listOf())
        state.getRemainingScore(501) shouldBe 501
    }

    @Test
    fun `Should correctly compute the current remaining score, taking into account busts`()
    {
        val roundOne = listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)) //121
        val roundTwo = listOf(Dart(20, 3), Dart(20, 3)) //bust, 121
        val roundThree = listOf(Dart(20, 3), Dart(20, 1), Dart(1, 1)) //40
        val roundFour = listOf(Dart(20, 1), Dart(10, 1), Dart(5, 1)) //5
        val roundFive = listOf(Dart(20, 1)) //bust, 5
        val roundSix = listOf(Dart(1, 1)) //4, mercied

        val rounds = makeX01Rounds(301, roundOne, roundTwo, roundThree, roundFour, roundFive, roundSix)

        val state = makeX01PlayerStateWithRounds(dartsThrown = rounds)
        state.getRemainingScoreForRound(301, 1) shouldBe 121
        state.getRemainingScoreForRound(301, 2) shouldBe 121
        state.getRemainingScoreForRound(301, 3) shouldBe 40
        state.getRemainingScoreForRound(301, 4) shouldBe 5
        state.getRemainingScoreForRound(301, 5) shouldBe 5
        state.getRemainingScoreForRound(301, 6) shouldBe 4

        state.getRemainingScore(301) shouldBe 4
    }

    @Test
    fun `Should return a bad luck count of 0 if no darts thrown`()
    {
        val state = makeX01PlayerStateWithRounds(dartsThrown = emptyList())
        state.getBadLuckCount() shouldBe 0
    }

    @Test
    fun `Should compute bad luck count correctly based on all thrown darts`()
    {
        val roundOne = listOf(
            makeDart(startingScore = 40, score = 5, multiplier = 2), //bad luck
            makeDart(startingScore = 30, score = 10, multiplier = 1),
            makeDart(startingScore = 20, score = 15, multiplier = 0)
        )

        val roundTwo = listOf(
            makeDart(startingScore = 20, score = 17, multiplier = 2)
        )

        val roundThree = listOf(
            makeDart(startingScore = 20, score = 15, multiplier = 2) //bad luck
        )

        val state = makeX01PlayerStateWithRounds(dartsThrown = listOf(roundOne, roundTwo, roundThree))
        state.getBadLuckCount() shouldBe 2

        state.dartThrown(makeDart(startingScore = 20, score = 6, multiplier = 2))
        state.getBadLuckCount() shouldBe 3

        state.resetRound()
        state.getBadLuckCount() shouldBe 2
    }
}