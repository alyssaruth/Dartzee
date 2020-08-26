package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import dartzee.`object`.DartHint
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test

class TestDartsScorerX01: AbstractScorerTest<DartsScorerX01>()
{
    override fun factoryScorerImpl() = DartsScorerX01(mockk(relaxed = true), "501")
    override fun addRound(scorer: DartsScorerX01, roundNumber: Int)
    {
        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(20, 2))
        scorer.addDart(Dart(20, 1))

        scorer.finaliseRoundScore(501 - (roundNumber - 1) * 60, false)
    }

    @Test
    fun `should have 4 columns`()
    {
        val scorer = factoryScorer()
        scorer.getNumberOfColumns() shouldBe 4
    }

    @Test
    fun `should clear the current round`()
    {
        val scorer = factoryScorer()

        addRound(scorer, 1)

        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(20, 1))

        scorer.getRowCount() shouldBe 2

        scorer.clearRound(2)

        scorer.getRowCount() shouldBe 1
        scorer.getTotalScore() shouldBe 3
    }

    @Test
    fun `should only report finalised rows as completed`()
    {
        val scorer = factoryScorer()

        scorer.addDart(Dart(20, 1))
        scorer.rowIsComplete(0).shouldBeFalse()

        scorer.finaliseRoundScore(501, false)
        scorer.rowIsComplete(0).shouldBeTrue()
    }

    @Test
    fun `should correctly report when a player is finished`()
    {
        val scorer = factoryScorer()

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
        val scorer = factoryScorer()

        scorer.addDart(Dart(20, 3))

        scorer.finaliseRoundScore(50, true)

        scorer.getLatestScoreRemaining() shouldBe 50
    }

    @Test
    fun `should return the latest score remaining`()
    {
        val scorer = factoryScorer()

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
        val scorer = factoryScorer()
        scorer.getLatestScoreRemaining() shouldBe 501
    }

    @Test
    fun `should include unthrown darts in the total score`()
    {
        val scorer = factoryScorer()

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
        val scorer = factoryScorer()
        scorer.addDart(Dart(1, 1))
        scorer.addHint(DartHint(1, 2))

        scorer.finaliseRoundScore(3, false)

        scorer.getDartsForRow(0).shouldContainExactly(Dart(1, 1))
        scorer.getLatestScoreRemaining() shouldBe 2
    }

    @Test
    fun `should not count hints in the dart total`()
    {
        val scorer = factoryScorer()

        scorer.addHint(DartHint(20, 1))
        scorer.addHint(DartHint(10, 2))

        scorer.getTotalScore() shouldBe 0
    }

    @Test
    fun `should remove hints when a real dart is added`()
    {
        val scorer = factoryScorer()

        scorer.addHint(DartHint(20, 1))
        scorer.addHint(DartHint(10, 2))

        scorer.addDart(Dart(10, 2))

        val dartCount = scorer.getTotalScore()
        dartCount shouldBe 1
    }
}