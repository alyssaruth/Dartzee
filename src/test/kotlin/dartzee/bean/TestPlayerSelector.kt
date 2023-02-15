package dartzee.bean

import com.github.alexburlton.swingtest.doubleClick
import dartzee.core.helper.processKeyPress
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.event.KeyEvent

class TestPlayerSelector: AbstractTest()
{
    @Test
    fun `Should start with no players selected`()
    {
        insertPlayer(name = "Alex")

        val selector = PlayerSelector()
        selector.init()

        getPlayerNamesToSelectFrom(selector).shouldContainExactly("Alex")
        selector.tablePlayersSelected.rowCount shouldBe 0
        selector.getSelectedPlayers().shouldBeEmpty()
    }

    @Test
    fun `Should return all selected players to unselected on init`()
    {
        val alex = insertPlayer(name = "Alex")

        val selector = PlayerSelector()
        selector.init(listOf(alex))

        selector.init()
        selector.getSelectedPlayers().shouldBeEmpty()
        getPlayerNamesToSelectFrom(selector).shouldContainExactly("Alex")
    }

    @Test
    fun `Should do nothing if the select button is clicked with no selection`()
    {
        insertPlayer()

        val selector = PlayerSelector()
        selector.init()

        selector.btnSelect.doClick()
        selector.getSelectedPlayers().shouldBeEmpty()
    }

    @Test
    fun `Should move the selected player across when the select button is clicked`()
    {
        insertPlayer(name = "Alex")
        insertPlayer(name = "Bob")
        insertPlayer(name = "Clive")

        val selector = PlayerSelector()
        selector.init()

        selector.tablePlayersToSelectFrom.selectRow(1)
        val playerToMove = selector.tablePlayersToSelectFrom.getSelectedPlayer()
        selector.btnSelect.doClick()

        selector.getSelectedPlayers().shouldContainExactly(playerToMove)
    }

    @Test
    fun `Should maintain selection in tables as players are moved`()
    {
        insertPlayer(name = "Alex")
        insertPlayer(name = "Bob")
        insertPlayer(name = "Clive")
        insertPlayer(name = "Derek")

        val selector = PlayerSelector()
        selector.init()

        selector.tablePlayersToSelectFrom.selectRow(1)

        selector.btnSelect.doClick()
        selector.tablePlayersToSelectFrom.selectedModelRow shouldBe 1

        selector.btnSelect.doClick()
        selector.tablePlayersToSelectFrom.selectedModelRow shouldBe 1

        selector.btnSelect.doClick()
        selector.tablePlayersToSelectFrom.selectedModelRow shouldBe 0

        selector.btnSelect.doClick()
        selector.tablePlayersToSelectFrom.selectedModelRow shouldBe -1
    }

    @Test
    fun `Should initialise with the selected players passed in`()
    {
        val alex = insertPlayer(name = "Alex")
        insertPlayer(name = "Bob")
        val clive = insertPlayer(name = "Clive")

        val selector = PlayerSelector()
        selector.init(listOf(alex, clive))

        selector.getSelectedPlayers().shouldContainExactly(alex, clive)
        getPlayerNamesToSelectFrom(selector).shouldContainExactly("Bob")
    }

    @Test
    fun `Should do nothing if the deselect button is clicked with no selection`()
    {
        val alex = insertPlayer(name = "Alex")
        insertPlayer(name = "Bob")
        insertPlayer(name = "Clive")

        val selector = PlayerSelector()
        selector.init(listOf(alex))

        selector.btnUnselect.doClick()

        selector.getSelectedPlayers().shouldContainExactly(alex)
    }

    @Test
    fun `Should move the selected player back when deselect button is clicked`()
    {
        val alex = insertPlayer(name = "Alex")
        val bob = insertPlayer(name = "Bob")
        val clive = insertPlayer(name = "Clive")

        val selector = PlayerSelector()
        selector.init(listOf(alex, bob, clive))

        selector.tablePlayersSelected.selectRow(2)
        selector.btnUnselect.doClick()

        selector.getSelectedPlayers().shouldContainExactly(alex, bob)
    }

    @Test
    fun `Should select players when Enter is pressed`()
    {
        insertPlayer("Alex")

        val selector = PlayerSelector()
        selector.init()

        selector.tablePlayersToSelectFrom.processKeyPress(KeyEvent.VK_ENTER)
        selector.getSelectedPlayers().size shouldBe 0

        selector.tablePlayersToSelectFrom.selectRow(0)
        selector.tablePlayersToSelectFrom.processKeyPress(KeyEvent.VK_ENTER)
        selector.getSelectedPlayers().size shouldBe 1
    }

    @Test
    fun `Should deselect players when Enter is pressed`()
    {
        val alex = insertPlayer("Alex")

        val selector = PlayerSelector()
        selector.init(listOf(alex))

        selector.tablePlayersSelected.processKeyPress(KeyEvent.VK_ENTER)
        selector.tablePlayersToSelectFrom.rowCount shouldBe 0

        selector.tablePlayersSelected.selectRow(0)
        selector.tablePlayersSelected.processKeyPress(KeyEvent.VK_ENTER)
        selector.tablePlayersToSelectFrom.rowCount shouldBe 1
    }

    @Test
    fun `Should select players on double-click`()
    {
        insertPlayer("Alex")

        val selector = PlayerSelector()
        selector.init()

        selector.tablePlayersToSelectFrom.selectRow(0)
        selector.tablePlayersToSelectFrom.doubleClick()
        selector.getSelectedPlayers().size shouldBe 1
    }

    @Test
    fun `Should deselect players on double-click`()
    {
        val alex = insertPlayer("Alex")

        val selector = PlayerSelector()
        selector.init(listOf(alex))

        selector.tablePlayersSelected.selectRow(0)
        selector.tablePlayersSelected.doubleClick()
        selector.tablePlayersToSelectFrom.rowCount shouldBe 1
    }

    private fun getPlayerNamesToSelectFrom(selector: PlayerSelector): List<String>
    {
        return selector.tablePlayersToSelectFrom.getAllPlayers().map { it.name }
    }
}