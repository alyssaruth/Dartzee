package dartzee.screen.stats.player.golf

import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.makeGolfRound
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestGolfStatsScorecard : AbstractTest()
{
    @Test
    fun `Should show the right number of columns`()
    {
        factoryScorecard(showGameIds = false).getNumberOfColumns() shouldBe 5
        factoryScorecard(showGameIds = true).getNumberOfColumns() shouldBe 6
    }

    @Test
    fun `Should render an extra column with gameIds`()
    {
        val scorer = factoryScorecard(showGameIds = true)
        scorer.getNumberOfColumns() shouldBe 6

        val roundOne = makeGolfRound(1, listOf(Dart(1, 3, segmentType = SegmentType.TREBLE)))
        val roundTwo = makeGolfRound(2, listOf(Dart(2, 0, segmentType = SegmentType.MISS), Dart(2, 1, segmentType = SegmentType.OUTER_SINGLE)))

        val gameIds = listOf(50L, 350L)
        scorer.populateTable(listOf(roundOne, roundTwo))
        scorer.addGameIds(gameIds)

        val rows = scorer.tableScores.getRows()
        rows.shouldContainExactlyInAnyOrder(
            listOf(1) + roundOne + listOf<Any?>(null, null, 2, 50L),
            listOf(2) + roundTwo + listOf<Any?>(null, 4, 350L)
        )
    }

    @Test
    fun `Should split gameIds if more than 9 rounds`()
    {
        val scorer = factoryScorecard(showGameIds = true)

        val rounds = (1..10).map { makeGolfRound(it, listOf(Dart(it, 2))) }
        scorer.populateTable(rounds)

        val gameIds = (1L..10L).toList()
        scorer.addGameIds(gameIds)

        scorer.tableScores.getValueAt(9, 5) shouldBe null
        scorer.tableScores.getValueAt(10, 5) shouldBe 10L
    }

    @Test
    fun `Should render a back 9`()
    {
        val roundOne = makeGolfRound(10, listOf(Dart(15, 1), Dart(10, 2, segmentType = SegmentType.DOUBLE)))
        val roundTwo = makeGolfRound(11, listOf(Dart(11, 1), Dart(11, 3, segmentType = SegmentType.TREBLE)))
        val scorer = factoryScorecard(9)
        scorer.populateTable(listOf(roundOne, roundTwo))

        val rows = scorer.tableScores.getRows()
        rows.shouldContainExactlyInAnyOrder(
            listOf(10) + roundOne + listOf<Any?>(null, 1),
            listOf(11) + roundTwo + listOf<Any?>(null, 2)
        )
    }

    private fun factoryScorecard(fudgeFactor: Int = 0, showGameIds: Boolean = false) = GolfStatsScorecard(fudgeFactor, showGameIds)
}