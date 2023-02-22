package dartzee.screen.player

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.bean.getAllPlayers
import dartzee.core.bean.ScrollTable
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.player.PlayerManager
import dartzee.utils.InjectedThings
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
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

        val table = scrn.getChild<ScrollTable>()
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

        val table = scrn.getChild<ScrollTable>()
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
        scrn.getChild<ScrollTable>().selectFirstRow()
        scrn.getSummaryPlayerName() shouldBe "Alex"

        scrn.initialise()
        scrn.getSummaryPlayerName() shouldBe ""
    }

    @Test
    fun `Should create a new human player and update the table`()
    {
        val playerManager = mockk<PlayerManager>()
        every { playerManager.createNewPlayer(any()) } answers { insertPlayer() }
        InjectedThings.playerManager = playerManager

        val scrn = PlayerManagementScreen()
        scrn.initialise()

        scrn.clickChild<JButton>("AddPlayer")
        verify { playerManager.createNewPlayer(true) }

        val table = scrn.getChild<ScrollTable>()
        table.rowCount shouldBe 1
    }

    @Test
    fun `Should create a new AI player and update the table`()
    {
        val playerManager = mockk<PlayerManager>()
        every { playerManager.createNewPlayer(any()) } answers { insertPlayer() }
        InjectedThings.playerManager = playerManager

        val scrn = PlayerManagementScreen()
        scrn.initialise()

        scrn.clickChild<JButton>("AddAi")
        verify { playerManager.createNewPlayer(false) }

        val table = scrn.getChild<ScrollTable>()
        table.rowCount shouldBe 1
    }

    @Test
    fun `Should not reinitialise the table if player creation is cancelled`()
    {
        val playerManager = mockk<PlayerManager>()
        every { playerManager.createNewPlayer(any()) } returns Unit
        InjectedThings.playerManager = playerManager

        val scrn = PlayerManagementScreen()
        scrn.initialise()

        insertPlayer()

        scrn.clickChild<JButton>("AddAi")
        scrn.clickChild<JButton>("AddPlayer")

        val table = scrn.getChild<ScrollTable>()
        table.rowCount shouldBe 0
    }

    private fun PlayerManagementScreen.getSummaryPlayerName(): String
    {
        val summaryPanel = getChild<PlayerManagementPanel>()
        return summaryPanel.lblPlayerName.text
    }
}