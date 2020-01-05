package burlton.dartzee.test.screen.game.scorer

import burlton.dartzee.code.`object`.*
import burlton.dartzee.code.screen.game.scorer.DartsScorerGolf
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartsScorerGolf: AbstractScorerTest<DartsScorerGolf>()
{
    override fun getValidGameParams() = "18"
    override fun factoryScorerImpl() = DartsScorerGolf()
    override fun addRound(scorer: DartsScorerGolf, roundNumber: Int)
    {
        val drt = Dart(roundNumber, 2)
        drt.segmentType = SEGMENT_TYPE_DOUBLE
        scorer.addDarts(listOf(drt))
    }

    @Test
    fun `Should show the right number of columns`()
    {
        val scorer = factoryScorer()
        scorer.getNumberOfColumns() shouldBe 5

        scorer.showGameId = true
        scorer.getNumberOfColumns() shouldBe 6
    }

    @Test
    fun `Should not clear the halfway score row`()
    {
        val scorer = factoryScorer()
        for (i in 1..9)
        {
            addRound(scorer, i)
        }

        scorer.getRowCount() shouldBe 10

        scorer.clearRound(10)
        scorer.getRowCount() shouldBe 10

        addRound(scorer, 10)
        scorer.getRowCount() shouldBe 11

        scorer.clearRound(10)
        scorer.getRowCount() shouldBe 10
    }

    @Test
    fun `Should correctly report whether a row is complete`()
    {
        val scorer = factoryScorer()

        scorer.addDart(Dart(1, 0))
        scorer.addDart(Dart(1, 0))
        scorer.addDart(Dart(1, 0))

        scorer.rowIsComplete(0) shouldBe false
        scorer.finaliseRoundScore()
        scorer.rowIsComplete(0) shouldBe true
    }

    @Test
    fun `Should add a subtotal row after 9 holes`()
    {
        val scorer = factoryScorer()
        for (i in 1..9)
        {
            addRound(scorer, i)
        }

        scorer.getRowCount() shouldBe 10
        scorer.getValueAt(9, 4) shouldBe 9
        scorer.getTotalScore() shouldBe 9

        addRound(scorer, 10)

        scorer.getValueAt(9, 4) shouldBe 9
        scorer.getTotalScore() shouldBe 10
    }

    @Test
    fun `Should compute the round score based on the last dart thrown`()
    {
        val dartOne = Dart(1, 3)
        val dartTwo = Dart(1, 0)
        val dartThree = Dart(1, 1)

        dartOne.segmentType = SEGMENT_TYPE_TREBLE
        dartTwo.segmentType = SEGMENT_TYPE_MISS
        dartThree.segmentType = SEGMENT_TYPE_OUTER_SINGLE

        val scorer = factoryScorer()
        scorer.addDart(dartOne)
        scorer.finaliseRoundScore()
        scorer.getValueAt(0, 4) shouldBe 2

        scorer.clearRound(1)
        scorer.addDart(dartOne)
        scorer.addDart(dartTwo)
        scorer.finaliseRoundScore()
        scorer.getValueAt(0, 4) shouldBe 5

        scorer.clearRound(1)
        scorer.addDart(dartOne)
        scorer.addDart(dartTwo)
        scorer.addDart(dartThree)
        scorer.finaliseRoundScore()
        scorer.getValueAt(0, 4) shouldBe 4
    }
}