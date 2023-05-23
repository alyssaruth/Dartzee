package dartzee.screen.player

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import dartzee.bean.PlayerAvatar
import dartzee.bean.getAllPlayers
import dartzee.clickCancel
import dartzee.clickOk
import dartzee.core.bean.ScrollTable
import dartzee.findWindow
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.randomGuid
import dartzee.screen.HumanConfigurationDialog
import dartzee.screen.ai.AIConfigurationDialog
import dartzee.typeText
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JButton
import javax.swing.JTextField

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
        val scrn = PlayerManagementScreen()
        scrn.initialise()

        scrn.clickChild<JButton>("AddPlayer")

        val dlg = findWindow<HumanConfigurationDialog>()
        dlg.shouldNotBeNull()

        dlg.getChild<JTextField>("nameField").typeText("Bongo")
        dlg.getChild<PlayerAvatar>().avatarId = randomGuid()
        dlg.clickOk()
        dlg.isVisible shouldBe false
        flushEdt()

        val table = scrn.getChild<ScrollTable>()
        table.rowCount shouldBe 1
    }

    @Test
    fun `Should create a new AI player and update the table`()
    {
        val scrn = PlayerManagementScreen()
        scrn.initialise()

        scrn.clickChild<JButton>("AddAi")

        val dlg = findWindow<AIConfigurationDialog>()
        dlg.shouldNotBeNull()

        dlg.getChild<JTextField>("nameField").typeText("Bingo")
        dlg.getChild<PlayerAvatar>().avatarId = randomGuid()
        dlg.clickOk()
        dlg.isVisible shouldBe false
        flushEdt()

        val table = scrn.getChild<ScrollTable>()
        table.rowCount shouldBe 1
    }

    @Test
    fun `Should not reinitialise the table if player creation is cancelled`()
    {
        val scrn = PlayerManagementScreen()
        scrn.initialise()

        insertPlayer()

        scrn.clickChild<JButton>("AddAi")
        val dlg = findWindow<AIConfigurationDialog>()
        dlg.shouldNotBeNull()
        dlg.clickCancel()

        scrn.clickChild<JButton>("AddPlayer")
        val humanDlg = findWindow<HumanConfigurationDialog>()
        humanDlg.shouldNotBeNull()
        humanDlg.clickCancel()

        flushEdt()
        val table = scrn.getChild<ScrollTable>()
        table.rowCount shouldBe 0
    }

    private fun PlayerManagementScreen.getSummaryPlayerName(): String
    {
        val summaryPanel = getChild<PlayerManagementPanel>()
        return summaryPanel.lblPlayerName.text
    }
}