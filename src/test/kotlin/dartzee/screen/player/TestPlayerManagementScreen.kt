package dartzee.screen.player

import dartzee.bean.getAllPlayers
import dartzee.core.bean.ScrollTable
import dartzee.findComponent
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test

class TestPlayerManagementScreen: AbstractTest()
{
    @Test
    fun `Should load players from the database`()
    {
        val p1 = insertPlayer()
        val p2 = insertPlayer()

        val scrn = PlayerManagementScreen()
        scrn.initialise()

        val table = scrn.findComponent<ScrollTable>()
        table.getAllPlayers().map { it.rowId }.shouldContainExactly(p1.rowId, p2.rowId)
    }

    @Test
    fun `Should refresh the summary panel as rows are selected or deselected`()
    {
        insertPlayer(name = "Alex")
        insertPlayer(name = "Leah")

        val scrn = PlayerManagementScreen()
        scrn.initialise()
        scrn.getSummaryPlayerName() shouldBe ""

        val table = scrn.findComponent<ScrollTable>()
        table.selectRow(0)
        scrn.getSummaryPlayerName() shouldBe "Alex"

        table.selectRow(1)
        scrn.getSummaryPlayerName() shouldBe "Leah"

        table.selectRow(-1)
        scrn.getSummaryPlayerName() shouldBe ""
    }

    @Test
    fun `Should reset the summary panel when reinitialised`()
    {
        insertPlayer(name = "Alex")

        val scrn = PlayerManagementScreen()
        scrn.initialise()
        scrn.findComponent<ScrollTable>().selectFirstRow()
        scrn.getSummaryPlayerName() shouldBe "Alex"

        scrn.initialise()
        scrn.getSummaryPlayerName() shouldBe ""
    }

    private fun PlayerManagementScreen.getSummaryPlayerName(): String
    {
        val summaryPanel = findComponent<PlayerManagementPanel>()
        return summaryPanel.lblPlayerName.text
    }
}