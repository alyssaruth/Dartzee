package dartzee.bean

import dartzee.core.bean.ScrollTable
import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.ListSelectionModel

class TestScrollTablePlayers: AbstractTest()
{
    @Test
    fun `Should initialise correctly`()
    {
        val table = ScrollTable()

        val bob = insertPlayer(name = "Bob", strategy = "")
        val robot = insertPlayer(name = "Robot", strategy = "foo")
        val players = listOf(bob, robot)
        table.initPlayerTableModel(players)


        table.getColumnName(0) shouldBe ""
        table.getColumnName(1) shouldBe "Player"
        table.rowCount shouldBe 2

        table.getValueAt(0, 0) shouldBe PlayerEntity.ICON_HUMAN
        table.getValueAt(0, 1) shouldBe bob

        table.getValueAt(1, 0) shouldBe PlayerEntity.ICON_AI
        table.getValueAt(1, 1) shouldBe robot
    }

    @Test
    fun `Should return all players correctly`()
    {
        val table = ScrollTable()

        val players = listOf(insertPlayer(name = "Bob", strategy = ""), insertPlayer(name = "Robot", strategy = "foo"))
        table.initPlayerTableModel(players)

        table.getAllPlayers() shouldBe players
    }

    @Test
    fun `Should return null and empty list if no selection`()
    {
        val table = ScrollTable()

        val players = listOf(insertPlayer(name = "Bob", strategy = ""), insertPlayer(name = "Robot", strategy = "foo"))
        table.initPlayerTableModel(players)

        table.getSelectedPlayer() shouldBe null
        table.getSelectedPlayers().shouldBeEmpty()
    }

    @Test
    fun `Should return the selected player`()
    {
        val table = ScrollTable()

        val playerTwo = insertPlayer(name = "Robot", strategy = "foo")
        val players = listOf(insertPlayer(name = "Bob", strategy = ""), playerTwo)
        table.initPlayerTableModel(players)

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.selectRow(1)

        table.getSelectedPlayer() shouldBe playerTwo
        table.getSelectedPlayers() shouldBe listOf(playerTwo)
    }

    @Test
    fun `Should return all the selected players`()
    {
        val table = ScrollTable()

        val playerOne = insertPlayer(name = "Alice")
        val playerTwo = insertPlayer(name = "Bob")
        val playerThree = insertPlayer(name = "Clive")

        table.initPlayerTableModel(listOf(playerOne, playerTwo, playerThree))

        table.selectRows(0, 1)

        table.getSelectedPlayers() shouldBe listOf(playerOne, playerTwo)
    }

    @Test
    fun `Should sort by name by default`()
    {
        val table = ScrollTable()

        val playerOne = insertPlayer(name = "Alice")
        val playerTwo = insertPlayer(name = "Bob")
        val playerThree = insertPlayer(name = "Clive")

        table.initPlayerTableModel(listOf(playerThree, playerOne, playerTwo))

        table.getAllPlayers() shouldBe listOf(playerOne, playerTwo, playerThree)
    }

    @Test
    fun `Should be able to add players dynamically`()
    {
        val table = ScrollTable()

        val playerOne = insertPlayer(name = "Alice")
        val playerTwo = insertPlayer(name = "Bob")
        val playerThree = insertPlayer(name = "Clive")

        table.initPlayerTableModel(listOf(playerOne))
        table.addPlayers(listOf(playerTwo, playerThree))

        table.getAllPlayers() shouldBe listOf(playerOne, playerTwo, playerThree)
    }

    private fun ScrollTable.selectRows(first: Int, last: Int)
    {
        table.setRowSelectionInterval(first, last)
    }

}