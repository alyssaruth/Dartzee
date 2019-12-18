package burlton.dartzee.test.screen.game.scorer

import burlton.core.test.helper.verifyNotCalled
import burlton.dartzee.code.screen.game.GamePanelDartzee
import burlton.dartzee.code.screen.game.scorer.DartsScorerDartzee
import burlton.dartzee.test.doClick
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertGame
import burlton.desktopcore.code.util.DateStatics
import burlton.desktopcore.code.util.getSqlDateNow
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

}