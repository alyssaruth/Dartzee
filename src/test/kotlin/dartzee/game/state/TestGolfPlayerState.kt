package dartzee.game.state

import dartzee.helper.*
import io.kotlintest.shouldBe
import org.junit.Test

class TestGolfPlayerState: AbstractTest()
{
    @Test
    fun `Should report a score of 0 when no darts thrown`()
    {
        val state = makeGolfPlayerState()
        state.getScoreSoFar() shouldBe 0
    }

    @Test
    fun `Should only take into account committed darts`()
    {
        val state = makeGolfPlayerState()
        state.dartThrown(makeDart(1, 1, golfHole = 1))
        state.dartThrown(makeDart(1, 2, golfHole = 1))
        state.getScoreSoFar() shouldBe 0

        state.commitRound()
        state.getScoreSoFar() shouldBe 1
    }

    @Test
    fun `Should sum the latest dart thrown from each round`()
    {
        val roundOne = makeGolfRound(1, listOf(makeDart(1, 1), makeDart(1, 3)))
        val roundTwo = makeGolfRound(2, listOf(makeDart(2, 0), makeDart(2, 1), makeDart(17, 1)))

        val state = makeGolfPlayerStateWithRounds(dartsThrown = listOf(roundOne, roundTwo))
        state.getScoreSoFar() shouldBe 7
    }

}