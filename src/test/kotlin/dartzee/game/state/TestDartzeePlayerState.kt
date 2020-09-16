package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.dartzee.DartzeeRoundResult
import dartzee.helper.AbstractTest
import dartzee.helper.makeDartzeePlayerState
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeePlayerState: AbstractTest()
{
    @Test
    fun `Should report a score of 0 when no darts have been thrown`()
    {
        val state = makeDartzeePlayerState(dartsThrown = emptyList())
        state.getScoreSoFar() shouldBe 0
    }

    @Test
    fun `Should correctly calculate the cumulative score for a given round`()
    {
        val scoringRound = listOf(Dart(20, 1), Dart(7, 3), Dart(19, 1))
        val resultTwo = DartzeeRoundResult(1, false, -30)
        val resultThree = DartzeeRoundResult(7, true, 75)
        val resultFour = DartzeeRoundResult(2, false, -52)
        val resultFive = DartzeeRoundResult(3, true, 50)
        val state = makeDartzeePlayerState(dartsThrown = listOf(scoringRound, emptyList(), emptyList(), emptyList(), emptyList()), roundResults = listOf(resultTwo, resultThree, resultFour, resultFive))

        state.getCumulativeScore(1) shouldBe 60
        state.getCumulativeScore(2) shouldBe 30
        state.getCumulativeScore(3) shouldBe 105
        state.getCumulativeScore(4) shouldBe 53
        state.getCumulativeScore(5) shouldBe 103
        state.getPeakScore() shouldBe 105
        state.getScoreSoFar() shouldBe 103
    }
}