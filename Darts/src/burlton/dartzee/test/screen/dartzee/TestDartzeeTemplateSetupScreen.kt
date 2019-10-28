package burlton.dartzee.test.screen.dartzee

import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.screen.dartzee.DartzeeTemplateSetupScreen
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertDartzeeTemplate
import burlton.dartzee.test.helper.insertGame
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeTemplateSetupScreen: AbstractDartsTest()
{
    @Test
    fun `Should pull through a game count of 0 for templates where no games have been played`()
    {
        insertDartzeeTemplate()

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getGameCount(0) shouldBe 0
    }

    @Test
    fun `Should pull through the right game count of for templates where games have been played`()
    {
        val templateId = insertDartzeeTemplate().rowId

        insertGame(gameType = GAME_TYPE_DARTZEE, gameParams = templateId)
        insertGame(gameType = GAME_TYPE_DARTZEE, gameParams = templateId)

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getGameCount(0) shouldBe 2
    }

    private fun DartzeeTemplateSetupScreen.getGameCount(row: Int): Int
    {
        return scrollTable.getValueAt(row, 3) as Int
    }
}