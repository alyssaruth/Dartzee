package dartzee.screen.game.scorer

import com.github.alexburlton.swingtest.clickChild
import dartzee.`object`.Dart
import dartzee.`object`.DartHint
import dartzee.core.util.DateStatics
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.helper.makeX01PlayerStateWithRounds
import dartzee.helper.makeX01Rounds
import dartzee.utils.ResourceCache.ICON_PAUSE
import dartzee.utils.ResourceCache.ICON_RESUME
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test
import javax.swing.JButton

class TestDartsScorerX01: AbstractTest()
{
    @Test
    fun `should have 4 columns`()
    {
        val scorer = factoryScorer()
        scorer.getNumberOfColumns() shouldBe 4
    }

    @Test
    fun `Should cope with empty state`()
    {
        val scorer = factoryScorer()

        val state = makeX01PlayerStateWithRounds()
        scorer.stateChanged(state)
        scorer.tableScores.rowCount shouldBe 0
    }

    @Test
    fun `Should render historic rounds correctly`()
    {
        val scorer = factoryScorer()

        val roundOne = listOf(Dart(20, 1), Dart(1, 1), Dart(20, 0))
        val roundTwo = listOf(Dart(20, 3), Dart(20, 1), Dart(20, 1))

        val rounds = makeX01Rounds(501, roundOne, roundTwo)
        val state = makeX01PlayerStateWithRounds(501, completedRounds = rounds)
        scorer.stateChanged(state)

        val rows = scorer.tableScores.getRows()
        rows.shouldContainExactly(
                roundOne + 480,
                roundTwo + 380
        )

        scorer.lblResult.text shouldBe "6 Darts"
    }

    @Test
    fun `Should include the current round`()
    {
        val scorer = factoryScorer()

        val roundOne = listOf(Dart(20, 1), Dart(1, 1), Dart(20, 0))

        val rounds = makeX01Rounds(501, roundOne)
        val state = makeX01PlayerStateWithRounds(501, completedRounds = rounds)
        state.dartThrown(Dart(20, 1))
        state.dartThrown(Dart(20, 1))
        scorer.stateChanged(state)

        val rows = scorer.tableScores.getRows()
        rows.shouldContainExactly(
                roundOne + 480,
                listOf(Dart(20, 1), Dart(20, 1), null, null)
        )

        scorer.lblResult.text shouldBe "5 Darts"
    }

    @Test
    fun `Should include checkout suggestions when appropriate`()
    {
        val scorer = factoryScorer()
        val state = makeX01PlayerStateWithRounds(101)
        scorer.stateChanged(state)

        scorer.tableScores.getRows().first().shouldContainExactly(
                DartHint(17, 3), DartHint(25, 2), null, null
        )

        state.dartThrown(Dart(20, 3))
        scorer.stateChanged(state)
        scorer.tableScores.getRows().first().shouldContainExactly(
                Dart(20, 3), DartHint(1, 1), DartHint(20, 2), null
        )

        state.dartThrown(Dart(20, 1))
        scorer.stateChanged(state)
        scorer.tableScores.getRows().first().shouldContainExactly(
                Dart(20, 3), Dart(20, 1), null, null
        )
    }

    @Test
    fun `Should toggle checkout suggestions on pause`()
    {
        val participant = insertParticipant(finishingPosition = 4, dtFinished = DateStatics.END_OF_TIME)
        val state = makeX01PlayerStateWithRounds(101, participant = participant)

        val scorer = factoryScorer()
        scorer.stateChanged(state)

        scorer.getPaused() shouldBe true
        scorer.tableScores.rowCount shouldBe 0

        //Unpause
        scorer.clickChild<JButton> { it.icon == ICON_RESUME }
        scorer.getPaused() shouldBe false
        scorer.tableScores.getRows().first().shouldContainExactly(
                DartHint(17, 3), DartHint(25, 2), null, null
        )

        //Pause again
        scorer.clickChild<JButton> { it.icon == ICON_PAUSE }
        scorer.getPaused() shouldBe true
        scorer.tableScores.rowCount shouldBe 0
    }

    private fun factoryScorer(): DartsScorerX01
    {
        val scorer = DartsScorerX01(mockk(relaxed = true), "501")
        scorer.init(null)
        return scorer
    }
}