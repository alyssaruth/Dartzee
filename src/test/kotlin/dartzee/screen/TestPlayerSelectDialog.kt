package dartzee.screen

import dartzee.bean.getAllPlayers
import dartzee.core.helper.doubleClick
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.ListSelectionModel

class TestPlayerSelectDialog: AbstractTest()
{
    @Test
    fun `Should not display excluded players`()
    {
        val bob = insertPlayer(name = "Bob")
        insertPlayer(name = "Alice")

        val dlg = PlayerSelectDialog(ListSelectionModel.SINGLE_SELECTION)
        dlg.playersToExclude = listOf(bob)
        dlg.buildTable()

        val players = getAvailablePlayerNames(dlg)
        players.shouldContainExactly("Alice")
    }

    @Test
    fun `Should respond to radio button selection`()
    {
        insertPlayer(name = "AI", strategy = "foo")
        insertPlayer(name = "Bob", strategy = "")

        val dlg = PlayerSelectDialog(ListSelectionModel.SINGLE_SELECTION)
        dlg.buildTable()

        getAvailablePlayerNames(dlg).shouldContainExactlyInAnyOrder("AI", "Bob")

        dlg.panelNorth.rdbtnAi.doClick()
        getAvailablePlayerNames(dlg).shouldContainExactly("AI")

        dlg.panelNorth.rdbtnHuman.doClick()
        getAvailablePlayerNames(dlg).shouldContainExactly("Bob")
    }

    @Test
    fun `Should show an error if okayed with no selection`()
    {
        val dlg = PlayerSelectDialog(ListSelectionModel.SINGLE_SELECTION)
        dlg.buildTable()

        dlg.btnOk.doClick()
        dialogFactory.errorsShown.shouldContainExactly("You must select at least one player.")
    }

    @Test
    fun `Should not show an error and update selectedPlayers when okayed with a selection`()
    {
        insertPlayer(name = "Bob")

        val dlg = PlayerSelectDialog(ListSelectionModel.SINGLE_SELECTION)
        dlg.buildTable()

        dlg.tablePlayers.selectRow(0)
        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldBeEmpty()
        dlg.selectedPlayers.size shouldBe 1
    }

    @Test
    fun `Should respond to double-click`()
    {
        insertPlayer(name = "Bob")

        val dlg = PlayerSelectDialog(ListSelectionModel.SINGLE_SELECTION)
        dlg.buildTable()

        dlg.tablePlayers.selectRow(0)
        dlg.tablePlayers.doubleClick()

        dialogFactory.errorsShown.shouldBeEmpty()
        dlg.selectedPlayers.size shouldBe 1
    }

    private fun getAvailablePlayerNames(dlg: PlayerSelectDialog): List<String>
    {
        val players = dlg.tablePlayers.getAllPlayers()

        return players.map{ it.name }
    }

}