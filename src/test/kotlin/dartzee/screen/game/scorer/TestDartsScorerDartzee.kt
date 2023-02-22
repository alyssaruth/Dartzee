package dartzee.screen.game.scorer

import com.github.alyssaburlton.swingtest.doClick
import dartzee.core.helper.verifyNotCalled
import dartzee.core.util.DateStatics
import dartzee.core.util.getSqlDateNow
import dartzee.dartzee.DartzeeRoundResult
import dartzee.game.state.IWrappedParticipant
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import dartzee.helper.makeDartzeePlayerState
import dartzee.helper.preparePlayers
import dartzee.`object`.Dart
import dartzee.screen.game.dartzee.GamePanelDartzee
import dartzee.screen.game.makeSingleParticipant
import dartzee.screen.game.makeTeam
import dartzee.shouldHaveColours
import dartzee.utils.DartsColour
import dartzee.utils.factoryHighScoreResult
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestDartsScorerDartzee: AbstractTest()
{
    @Test
    fun `Should listen to mouse events once the game has finished`()
    {
        val game = insertGame(dtFinish = getSqlDateNow())
        val parent = mockk<GamePanelDartzee>(relaxed = true)
        every { parent.gameEntity } returns game

        val scorer = makeScorer(parent)
        scorer.lblAvatar.doClick()

        verify { parent.scorerSelected(scorer) }
    }

    @Test
    fun `Should not pass on mouse clicks if the game is ongoing`()
    {
        val game = insertGame(dtFinish = DateStatics.END_OF_TIME)
        val parent = mockk<GamePanelDartzee>(relaxed = true)
        every { parent.gameEntity } returns game

        val scorer = makeScorer(parent)
        scorer.lblAvatar.doClick()

        verifyNotCalled { parent.scorerSelected(scorer) }
    }

    @Test
    fun `Should be selectable post-game`()
    {
        val (p1, p2) = preparePlayers(2)
        val team = makeTeam(p1, p2)
        val scorer = makeScorer(participant = team)

        scorer.togglePostGame(true)
        scorer.lblName.text shouldBe "<html><b>Alice &#38; Bob</b></html>"

        scorer.togglePostGame(false)
        scorer.lblName.text shouldBe "<html>Alice &#38; Bob</html>"
    }

    @Test
    fun `Should return 0 for score so far when no entries`()
    {
        val scorer = makeScorer()
        scorer.lblResult.text shouldBe ""
    }

    @Test
    fun `Should set the score and finishing position`()
    {
        val scorer = makeScorer()
        val pt = insertParticipant(finishingPosition = 2)

        val roundOne = listOf(Dart(20, 1), Dart(20, 2), Dart(20, 3))
        val roundTwo = listOf(Dart(5, 1), Dart(5, 1), Dart(5, 1))
        val resultOne = DartzeeRoundResult(1, false, -60)

        val state = makeDartzeePlayerState(pt, listOf(roundOne, roundTwo), listOf(resultOne))
        scorer.stateChanged(state)

        scorer.lblResult.text shouldBe "60"
        scorer.lblResult.shouldHaveColours(DartsColour.SECOND_COLOURS)
    }

    @Test
    fun `Table should contain the correct darts and results for completed rounds`()
    {
        val scorer = makeScorer()

        val roundOne = listOf(Dart(20, 1), Dart(20, 2), Dart(20, 3)) //120
        val roundTwo = listOf(Dart(5, 1), Dart(5, 1), Dart(5, 1)) //60
        val roundThree = listOf(Dart(25, 2), Dart(10, 1), Dart(12, 1)) //110
        val resultOne = DartzeeRoundResult(1, false, -60)
        val resultTwo = DartzeeRoundResult(7, true, 50)

        val state = makeDartzeePlayerState(insertParticipant(), listOf(roundOne, roundTwo, roundThree), listOf(resultOne, resultTwo))
        scorer.stateChanged(state)

        val rows = scorer.tableScores.getRows()
        rows.shouldContainExactly(
                roundOne + factoryHighScoreResult(roundOne) + 120,
                roundTwo + resultOne + 60,
                roundThree + resultTwo + 110
        )
    }

    @Test
    fun `Should include the in progress round`()
    {
        val scorer = makeScorer()

        val roundOne = listOf(Dart(20, 1), Dart(20, 2), Dart(20, 3)) //120
        val state = makeDartzeePlayerState(insertParticipant(), listOf(roundOne))
        state.dartThrown(Dart(5, 1))
        state.dartThrown(Dart(10, 1))
        scorer.stateChanged(state)

        val rows = scorer.tableScores.getRows()
        rows.shouldContainExactly(
                roundOne + factoryHighScoreResult(roundOne) + 120,
                listOf(Dart(5, 1), Dart(10, 1), null, null, null)
        )
    }

    @Test
    fun `Should update the result renderer based on the current maximum score`()
    {
        val scorer = makeScorer()

        val roundOne = listOf(Dart(20, 1), Dart(20, 2), Dart(20, 3))
        val roundTwo = listOf(Dart(5, 1), Dart(5, 1), Dart(5, 1))
        val resultOne = DartzeeRoundResult(1, false, -60)

        val state = makeDartzeePlayerState(insertParticipant(), listOf(roundOne, roundTwo), listOf(resultOne))
        scorer.stateChanged(state)
        scorer.getRendererMaximum() shouldBe 120

        val otherResult = DartzeeRoundResult(5, true, 60)
        val improvedState = makeDartzeePlayerState(insertParticipant(), listOf(roundOne, roundTwo), listOf(otherResult))
        scorer.stateChanged(improvedState)
        scorer.getRendererMaximum() shouldBe 180
    }

    @Test
    fun `Table should have the right columns and renderers`()
    {
        val scorer = makeScorer()

        scorer.getNumberOfColumns() shouldBe 5

        scorer.tableScores.getColumn(0).cellRenderer.shouldBeInstanceOf<DartRenderer>()
        scorer.tableScores.getColumn(1).cellRenderer.shouldBeInstanceOf<DartRenderer>()
        scorer.tableScores.getColumn(2).cellRenderer.shouldBeInstanceOf<DartRenderer>()
        scorer.tableScores.getColumn(3).cellRenderer.shouldBeInstanceOf<DartzeeRoundResultRenderer>()
    }

    @Test
    fun `Should cope with empty state`()
    {
        val scorer = makeScorer()

        val state = makeDartzeePlayerState()
        scorer.stateChanged(state)
        scorer.tableScores.rowCount shouldBe 0
    }

    private fun makeScorer(parent: GamePanelDartzee = mockk(), participant: IWrappedParticipant = makeSingleParticipant()) =
        DartsScorerDartzee(parent, participant).also { it.init() }

    private fun DartsScorerDartzee.getRendererMaximum(): Int
    {
        val renderer = tableScores.getColumn(4).cellRenderer
        renderer.shouldBeInstanceOf<DartzeeScoreRenderer>()
        return renderer.maxScore
    }
}