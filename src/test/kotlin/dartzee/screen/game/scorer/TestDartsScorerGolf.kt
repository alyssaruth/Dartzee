package dartzee.screen.game.scorer

import com.github.alexburlton.swingtest.shouldMatchImage
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.game.state.GolfPlayerState
import dartzee.getRows
import dartzee.helper.*
import dartzee.shouldHaveColours
import dartzee.utils.DartsColour
import dartzee.wrapInFrame
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Dimension

class TestDartsScorerGolf: AbstractTest()
{
    @Test
    fun `Should show the right number of columns`()
    {
        val scorer = factoryScorer()
        scorer.getNumberOfColumns() shouldBe 5

        scorer.showGameId = true
        scorer.getNumberOfColumns() shouldBe 6
    }

    @Test
    fun `Should add a subtotal row after 9 and 18 holes`()
    {
        val scorer = factoryScorer()
        val state = makeGolfPlayerState()
        for (i in 1..9)
        {
            addRound(state, i)
        }

        scorer.stateChanged(state)
        scorer.tableScores.rowCount shouldBe 10
        scorer.tableScores.getValueAt(9, 4) shouldBe 9
        scorer.lblResult.text shouldBe "9"

        addRound(state, 10)

        scorer.stateChanged(state)
        scorer.tableScores.getValueAt(9, 4) shouldBe 9
        scorer.lblResult.text shouldBe "10"

        for (i in 11..18)
        {
            addRound(state, i)
        }

        scorer.stateChanged(state)
        scorer.tableScores.getValueAt(9, 4) shouldBe 9
        scorer.tableScores.getValueAt(19, 4) shouldBe 18
        scorer.lblResult.text shouldBe "18"
    }
    private fun addRound(state: GolfPlayerState, hole: Int)
    {
        val drt = Dart(hole, 2, segmentType = SegmentType.DOUBLE)
        drt.roundNumber = hole
        state.addCompletedRound(listOf(drt))
    }

    @Test
    fun `Should set the score and finishing position`()
    {
        val scorer = factoryScorer()
        scorer.init(insertPlayer())
        val pt = insertParticipant(finishingPosition = 2)

        val roundOne = makeGolfRound(1, listOf(Dart(1, 3, segmentType = SegmentType.TREBLE)))
        val roundTwo = makeGolfRound(2, listOf(Dart(2, 0, segmentType = SegmentType.MISS), Dart(2, 1, segmentType = SegmentType.OUTER_SINGLE)))
        val roundThree = makeGolfRound(3, listOf(Dart(3, 0, segmentType = SegmentType.MISS), Dart(3, 0, segmentType = SegmentType.MISS), Dart(3, 0, segmentType = SegmentType.MISS)))

        val state = makeGolfPlayerState(participant = pt, completedRounds = listOf(roundOne, roundTwo, roundThree))
        scorer.stateChanged(state)

        scorer.lblResult.text shouldBe "11"
        scorer.lblResult.shouldHaveColours(DartsColour.SECOND_COLOURS)
    }

    @Test
    fun `Should render completed rounds correctly`()
    {
        val roundOne = makeGolfRound(1, listOf(Dart(1, 3, segmentType = SegmentType.TREBLE)))
        val roundTwo = makeGolfRound(2, listOf(Dart(2, 0, segmentType = SegmentType.MISS), Dart(2, 1, segmentType = SegmentType.OUTER_SINGLE)))
        val roundThree = makeGolfRound(3, listOf(Dart(3, 0, segmentType = SegmentType.MISS), Dart(3, 0, segmentType = SegmentType.MISS), Dart(3, 0, segmentType = SegmentType.MISS)))

        val state = makeGolfPlayerState(completedRounds = listOf(roundOne, roundTwo, roundThree))

        val scorer = factoryScorer()
        scorer.stateChanged(state)

        val rows = scorer.tableScores.getRows()
        rows.shouldContainExactlyInAnyOrder(
                listOf(1) + roundOne + listOf(null, null) + 2,
                listOf(2) + roundTwo + listOf(null) + 4,
                listOf(3) + roundThree + 5
        )
    }

    @Test
    fun `Should render the current round`()
    {
        val roundOne = makeGolfRound(1, listOf(Dart(1, 3, segmentType = SegmentType.TREBLE)))

        val state = makeGolfPlayerState(completedRounds = listOf(roundOne))
        state.dartThrown(Dart(2, 0))

        val scorer = factoryScorer()
        scorer.stateChanged(state)

        val rows = scorer.tableScores.getRows()
        rows.shouldContainExactlyInAnyOrder(
                listOf(1) + roundOne + listOf(null, null) + 2,
                listOf(2) + Dart(2, 0) + listOf(null, null, null)
        )
    }

    @Test
    fun `Should cope with empty state`()
    {
        val scorer = factoryScorer()

        val state = makeGolfPlayerState()
        scorer.stateChanged(state)
        scorer.tableScores.rowCount shouldBe 0
    }

    @Test
    fun `Should match screenshot - in progress`()
    {
        val scorer = factoryScorer()

        val roundOne = makeGolfRound(1, listOf(Dart(1, 3, segmentType = SegmentType.TREBLE)))
        val roundTwo = makeGolfRound(2, listOf(Dart(2, 0, segmentType = SegmentType.MISS), Dart(2, 1, segmentType = SegmentType.OUTER_SINGLE)))
        val roundThree = makeGolfRound(3, listOf(Dart(3, 0, segmentType = SegmentType.MISS), Dart(3, 0, segmentType = SegmentType.MISS), Dart(3, 0, segmentType = SegmentType.MISS)))

        val state = makeGolfPlayerState(completedRounds = listOf(roundOne, roundTwo, roundThree))
        state.dartThrown(Dart(20, 1))
        state.dartThrown(Dart(20, 1))
        scorer.stateChanged(state)

        val table = scorer.tableScores
        table.size = Dimension(200, 300)
        table.preferredSize = Dimension(200, 300)
        table.wrapInFrame().shouldMatchImage("in progress")
    }

    private fun factoryScorer(): DartsScorerGolf
    {
        val scorer = DartsScorerGolf()
        scorer.init(insertPlayer())
        return scorer
    }
}