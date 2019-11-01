package burlton.dartzee.test.screen.dartzee

import burlton.dartzee.code.db.DARTZEE_TEMPLATE
import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.screen.dartzee.DartzeeTemplateSetupScreen
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.getCountFromTable
import burlton.dartzee.test.helper.insertDartzeeTemplate
import burlton.dartzee.test.helper.insertGame
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test
import javax.swing.JOptionPane

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

    @Test
    fun `Should toggle copy & remove buttons based on whether a row is selected`()
    {
        insertDartzeeTemplate()

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.btnCopy.isEnabled shouldBe false
        scrn.btnDelete.isEnabled shouldBe false

        scrn.scrollTable.selectRow(0)
        scrn.btnCopy.isEnabled shouldBe true
        scrn.btnDelete.isEnabled shouldBe true

        scrn.scrollTable.selectRow(-1)
        scrn.btnCopy.isEnabled shouldBe false
        scrn.btnDelete.isEnabled shouldBe false
    }

    @Test
    fun `Should leave template alone if delete is cancelled`()
    {
        insertDartzeeTemplate(name = "ABC")
        dialogFactory.questionOption = JOptionPane.NO_OPTION

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.scrollTable.selectRow(0)
        scrn.btnDelete.doClick()

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete the ABC Template?")
        scrn.scrollTable.rowCount shouldBe 1
        getCountFromTable(DARTZEE_TEMPLATE) shouldBe 1
    }

    @Test
    fun `Should delete a template on confirmation`()
    {
        insertDartzeeTemplate(name = "ABC")
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.scrollTable.selectRow(0)
        scrn.btnDelete.doClick()

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete the ABC Template?")

        scrn.scrollTable.rowCount shouldBe 0
        getCountFromTable(DARTZEE_TEMPLATE) shouldBe 0
    }

    private fun DartzeeTemplateSetupScreen.getGameCount(row: Int): Int
    {
        return scrollTable.getValueAt(row, 3) as Int
    }
}