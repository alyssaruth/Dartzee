package dartzee.screen.player

import dartzee.bean.getAllPlayers
import dartzee.bean.getPlayerEntityForRow
import dartzee.clickComponent
import dartzee.core.bean.ScrollTable
import dartzee.findComponent
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.player.PlayerManager
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import javax.swing.JButton

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

    @Test
    fun `Should create a new human player and update the table`()
    {
        val playerManager = mockk<PlayerManager>()
        every { playerManager.createNewPlayer(any()) } returns true
        InjectedThings.playerManager = playerManager

        val scrn = PlayerManagementScreen()
        scrn.initialise()

        val player = insertPlayer()

        scrn.clickComponent<JButton>("", "Add player")
        verify { playerManager.createNewPlayer(true) }

        val table = scrn.findComponent<ScrollTable>()
        table.getPlayerEntityForRow(0).rowId shouldBe player.rowId
    }

    @Test
    fun `Should create a new AI player and update the table`()
    {
        val playerManager = mockk<PlayerManager>()
        every { playerManager.createNewPlayer(any()) } returns true
        InjectedThings.playerManager = playerManager

        val scrn = PlayerManagementScreen()
        scrn.initialise()

        val player = insertPlayer()

        scrn.clickComponent<JButton>("", "Add computer")
        verify { playerManager.createNewPlayer(false) }

        val table = scrn.findComponent<ScrollTable>()
        table.getPlayerEntityForRow(0).rowId shouldBe player.rowId
    }

    @Test
    fun `Should not reinitialise the table if player creation is cancelled`()
    {
        val playerManager = mockk<PlayerManager>()
        every { playerManager.createNewPlayer(any()) } returns false
        InjectedThings.playerManager = playerManager

        val scrn = PlayerManagementScreen()
        scrn.initialise()

        insertPlayer()

        scrn.clickComponent<JButton>("", "Add computer")
        scrn.clickComponent<JButton>("", "Add player")

        val table = scrn.findComponent<ScrollTable>()
        table.rowCount shouldBe 0
    }

    private fun PlayerManagementScreen.getSummaryPlayerName(): String
    {
        val summaryPanel = findComponent<PlayerManagementPanel>()
        return summaryPanel.lblPlayerName.text
    }
}