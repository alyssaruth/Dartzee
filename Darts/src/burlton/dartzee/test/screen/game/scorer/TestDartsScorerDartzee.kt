package burlton.dartzee.test.screen.game.scorer

import burlton.dartzee.test.core.helper.verifyNotCalled
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.screen.game.GamePanelDartzee
import burlton.dartzee.code.screen.game.scorer.DartRenderer
import burlton.dartzee.code.screen.game.scorer.DartsScorerDartzee
import burlton.dartzee.code.screen.game.scorer.DartzeeRoundResultRenderer
import burlton.dartzee.code.screen.game.scorer.DartzeeScoreRenderer
import burlton.dartzee.test.doClick
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertGame
import burlton.dartzee.test.helper.insertPlayer
import burlton.dartzee.code.core.util.DateStatics
import burlton.dartzee.code.core.util.getSqlDateNow
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestDartsScorerDartzee: AbstractDartsTest()
{
    @Test
    fun `Should listen to mouse events once the game has finished`()
    {
        val game = insertGame(dtFinish = getSqlDateNow())
        val parent = mockk<GamePanelDartzee>(relaxed = true)
        every { parent.gameEntity } returns game

        val scorer = DartsScorerDartzee(parent)
        scorer.lblAvatar.doClick()

        verify { parent.scorerSelected(scorer) }
    }

    @Test
    fun `Should not pass on mouse clicks if the game is ongoing`()
    {
        val game = insertGame(dtFinish = DateStatics.END_OF_TIME)
        val parent = mockk<GamePanelDartzee>(relaxed = true)
        every { parent.gameEntity } returns game

        val scorer = DartsScorerDartzee(parent)
        scorer.lblAvatar.doClick()

        verifyNotCalled { parent.scorerSelected(scorer) }
    }

    @Test
    fun `Should return 0 for score so far when no entries`()
    {
        val scorer = DartsScorerDartzee(mockk())
        scorer.getTotalScore() shouldBe 0
    }

    @Test
    fun `Should cope with partial rounds`()
    {
        val scorer = DartsScorerDartzee(mockk())
        scorer.init(insertPlayer(), "")

        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(20, 2))
        scorer.addDart(Dart(20, 3))

        scorer.getTotalScore() shouldBe 0
        scorer.lblResult.text shouldBe ""

        scorer.setResult(DartzeeRoundResult(2, true, 120))
        scorer.getTotalScore() shouldBe 120
        scorer.lblResult.text shouldBe "120"

        scorer.addDart(Dart(20, 1))
        scorer.getTotalScore() shouldBe 120

        scorer.setResult(DartzeeRoundResult(3, false, -60))
        scorer.getTotalScore() shouldBe 60
        scorer.lblResult.text shouldBe "60"
    }

    @Test
    fun `Should correctly report whether a row is complete`()
    {
        val scorer = DartsScorerDartzee(mockk())
        scorer.init(insertPlayer(), "")

        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(20, 2))
        scorer.addDart(Dart(20, 3))
        scorer.rowIsComplete(0) shouldBe false

        scorer.setResult(DartzeeRoundResult(2, true, 120))
        scorer.rowIsComplete(0) shouldBe true
    }

    @Test
    fun `Should update the result renderer based on the current maximum score`()
    {
        val scorer = DartsScorerDartzee(mockk())
        scorer.init(insertPlayer(), "")

        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(20, 2))
        scorer.addDart(Dart(20, 3))

        scorer.setResult(DartzeeRoundResult(2, true, 50))
        scorer.getRendererMaximum() shouldBe 50

        scorer.addDart(Dart(20, 1))

        scorer.setResult(DartzeeRoundResult(3, false, -25))
        scorer.getRendererMaximum() shouldBe 50

        scorer.addDart(Dart(15, 1))
        scorer.addDart(Dart(15, 1))
        scorer.addDart(Dart(15, 1))
        scorer.setResult(DartzeeRoundResult(4, true, 45))
        scorer.getRendererMaximum() shouldBe 70
    }

    @Test
    fun `Table should have the right columns and renderers`()
    {
        val scorer = DartsScorerDartzee(mockk())
        scorer.init(insertPlayer(), "")

        scorer.getNumberOfColumns() shouldBe 5

        scorer.tableScores.getColumn(0).cellRenderer.shouldBeInstanceOf<DartRenderer>()
        scorer.tableScores.getColumn(1).cellRenderer.shouldBeInstanceOf<DartRenderer>()
        scorer.tableScores.getColumn(2).cellRenderer.shouldBeInstanceOf<DartRenderer>()
        scorer.tableScores.getColumn(3).cellRenderer.shouldBeInstanceOf<DartzeeRoundResultRenderer>()
    }

    private fun DartsScorerDartzee.getRendererMaximum(): Int
    {
        val renderer = tableScores.getColumn(4).cellRenderer
        renderer.shouldBeInstanceOf<DartzeeScoreRenderer>()

        return (renderer as DartzeeScoreRenderer).maxScore
    }
}