package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartsScorerGolf: AbstractScorerTest<DartsScorerGolf>()
{
    override fun factoryScorerImpl() = DartsScorerGolf()
    override fun addRound(scorer: DartsScorerGolf, roundNumber: Int)
    {
        val drt = Dart(roundNumber, 2, segmentType = SegmentType.DOUBLE)
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
    fun `Should add a subtotal row after 9 holes`()
    {
        val scorer = factoryScorer()
        for (i in 1..9)
        {
            addRound(scorer, i)
        }

        scorer.getRowCount() shouldBe 10
        scorer.getValueAt(9, 4) shouldBe 9
        scorer.lblResult.text shouldBe "9"

        addRound(scorer, 10)

        scorer.getValueAt(9, 4) shouldBe 9
        scorer.lblResult.text shouldBe "10"
    }

    @Test
    fun `Should compute the round score based on the last dart thrown`()
    {
        val dartOne = Dart(1, 3, segmentType = SegmentType.TREBLE)
        val dartTwo = Dart(1, 0, segmentType = SegmentType.MISS)
        val dartThree = Dart(1, 1, segmentType = SegmentType.OUTER_SINGLE)

        val scorer = factoryScorer()
        scorer.addDart(dartOne)
        scorer.finaliseRoundScore()
        scorer.getValueAt(0, 4) shouldBe 2

        scorer.addDart(dartOne)
        scorer.addDart(dartTwo)
        scorer.finaliseRoundScore()
        scorer.getValueAt(0, 4) shouldBe 5

        scorer.addDart(dartOne)
        scorer.addDart(dartTwo)
        scorer.addDart(dartThree)
        scorer.finaliseRoundScore()
        scorer.getValueAt(0, 4) shouldBe 4
    }
}