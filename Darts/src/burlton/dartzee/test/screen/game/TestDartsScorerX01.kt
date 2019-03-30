package burlton.dartzee.test.screen.game

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartHint
import burlton.dartzee.code.screen.game.DartsScorerX01
import burlton.dartzee.test.helper.AbstractTest
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartsScorerX01: AbstractTest()
{
    @Test
    fun `should have 4 columns`()
    {
        val scorer = getTestScorer()
        scorer.numberOfColumns shouldBe 4
    }

    @Test
    fun `should clear the current round`()
    {
        val scorer = getTestScorer()

        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(20, 1))

        scorer.finaliseRoundScore(501, false)

        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(20, 1))

        scorer.clearCurrentRound()

        scorer.getTotalScore() shouldBe 3
    }

    @Test
    fun `should not clear finalised rounds`()
    {
        val scorer = getTestScorer()
        scorer.addDart(Dart(1, 1))

        scorer.finaliseRoundScore(501, false)

        scorer.clearCurrentRound()
        scorer.getDartsForRow(0).shouldContainExactly(Dart(1, 1))
    }

    @Test
    fun `should do nothing if asked to clear when empty`()
    {
        val scorer = getTestScorer()

        scorer.clearCurrentRound()
    }

    @Test
    fun `should only report finalised rows as completed`()
    {
        val scorer = getTestScorer()

        scorer.addDart(Dart(20, 1))
        scorer.rowIsComplete(0).shouldBeFalse()

        scorer.finaliseRoundScore(501, false)
        scorer.rowIsComplete(0).shouldBeTrue()
    }

    @Test
    fun `should correctly report when a player is finished`()
    {
        val scorer = getTestScorer()

        scorer.playerIsFinished().shouldBeFalse()

        scorer.addDart(Dart(20, 3))
        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(20, 0))

        scorer.finaliseRoundScore(120, false)

        scorer.playerIsFinished().shouldBeFalse()

        scorer.addDart(Dart(20, 2))

        scorer.finaliseRoundScore(40, false)
        scorer.playerIsFinished().shouldBeTrue()
    }

    @Test
    fun `should not update the score if bust`()
    {
        val scorer = getTestScorer()

        scorer.addDart(Dart(20, 3))

        scorer.finaliseRoundScore(50, true)

        scorer.getLatestScoreRemaining() shouldBe 50
    }

    @Test
    fun `should return the latest score remaining`()
    {
        val scorer = getTestScorer()

        scorer.addDart(Dart(20, 3))
        scorer.addDart(Dart(20, 3))
        scorer.addDart(Dart(20, 3))

        scorer.finaliseRoundScore(501, false)

        scorer.getLatestScoreRemaining() shouldBe 321

        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(1, 1))
        scorer.addDart(Dart(1, 0))
        scorer.finaliseRoundScore(321, false)

        scorer.getLatestScoreRemaining() shouldBe 300
    }

    @Test
    fun `should return the starting score if no darts thrown`()
    {
        val scorer = getTestScorer()
        scorer.getLatestScoreRemaining() shouldBe 501
    }

    @Test
    fun `should include unthrown darts in the total score`()
    {
        val scorer = getTestScorer()

        scorer.getTotalScore() shouldBe 0

        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(20, 1))
        scorer.getTotalScore() shouldBe 2

        scorer.finaliseRoundScore(501, false)
        scorer.getTotalScore() shouldBe 3

        scorer.addDart(Dart(19, 3))
        scorer.getTotalScore() shouldBe 4

        scorer.finaliseRoundScore(461, false)
        scorer.getTotalScore() shouldBe 6
    }

    @Test
    fun `should remove hints when score is confirmed`()
    {
        val scorer = getTestScorer()
        scorer.addDart(Dart(1, 1))
        scorer.addHint(DartHint(1, 2))

        scorer.finaliseRoundScore(3, false)

        scorer.getDartsForRow(0).shouldContainExactly(Dart(1, 1))
        scorer.getLatestScoreRemaining() shouldBe 2
    }

    @Test
    fun `should not count hints in the dart total`()
    {
        val scorer = getTestScorer()

        scorer.addHint(DartHint(20, 1))
        scorer.addHint(DartHint(10, 2))

        scorer.getTotalScore() shouldBe 0
    }

    @Test
    fun `should remove hints when a real dart is added`()
    {
        val scorer = getTestScorer()

        scorer.addHint(DartHint(20, 1))
        scorer.addHint(DartHint(10, 2))

        scorer.addDart(Dart(10, 2))

        val dartCount = scorer.getTotalScore()
        dartCount shouldBe 1
    }

    private fun getTestScorer(): DartsScorerX01
    {
        val scorer = DartsScorerX01()
        scorer.init(null, "501")
        return scorer
    }
}