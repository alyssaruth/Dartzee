package burlton.dartzee.test.bean

import burlton.dartzee.code.bean.PlayerSelector
import burlton.dartzee.test.helper.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.event.KeyEvent

class TestPlayerSelector: AbstractDartsTest()
{
    override fun beforeEachTest()
    {
        super.beforeEachTest()

        wipeTable("Player")
    }

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

        processKeyPress(selector.tablePlayersToSelectFrom, KeyEvent.VK_ENTER)
        selector.getSelectedPlayers().size shouldBe 0

        selector.tablePlayersToSelectFrom.selectRow(0)
        processKeyPress(selector.tablePlayersToSelectFrom, KeyEvent.VK_ENTER)
        selector.getSelectedPlayers().size shouldBe 1
    }

    @Test
    fun `Should deselect players when Enter is pressed`()
    {
        val alex = insertPlayer("Alex")

        val selector = PlayerSelector()
        selector.init(listOf(alex))

        processKeyPress(selector.tablePlayersSelected, KeyEvent.VK_ENTER)
        selector.tablePlayersToSelectFrom.rowCount shouldBe 0

        selector.tablePlayersSelected.selectRow(0)
        processKeyPress(selector.tablePlayersSelected, KeyEvent.VK_ENTER)
        selector.tablePlayersToSelectFrom.rowCount shouldBe 1
    }

    @Test
    fun `Should select players on double-click`()
    {
        insertPlayer("Alex")

        val selector = PlayerSelector()
        selector.init()

        selector.tablePlayersToSelectFrom.selectRow(0)
        doubleClick(selector.tablePlayersToSelectFrom)
        selector.getSelectedPlayers().size shouldBe 1
    }

    @Test
    fun `Should deselect players on double-click`()
    {
        val alex = insertPlayer("Alex")

        val selector = PlayerSelector()
        selector.init(listOf(alex))

        selector.tablePlayersSelected.selectRow(0)
        doubleClick(selector.tablePlayersSelected)
        selector.tablePlayersToSelectFrom.rowCount shouldBe 1
    }

    private fun getPlayerNamesToSelectFrom(selector: PlayerSelector): List<String>
    {
        return selector.tablePlayersToSelectFrom.getAllPlayers().map{ it.name }
    }

    /**
     * Valid
     */
    @Test
    fun `Should always be invalid if 0 players selected`()
    {
        val selector = PlayerSelector()
        selector.init()

        selector.valid(false) shouldBe false
        selector.valid(true) shouldBe false

        dialogFactory.errorsShown.shouldContain("You must select at least 1 player.")
    }

    @Test
    fun `Should be valid for 1 player if not a match`()
    {
        val alex = insertPlayer()

        val selector = PlayerSelector()
        selector.init(listOf(alex))

        selector.valid(false) shouldBe true
        dialogFactory.errorsShown.shouldBeEmpty()
    }

    @Test
    fun `Should be invalid for a match of 1 player`()
    {
        val alex = insertPlayer()

        val selector = PlayerSelector()
        selector.init(listOf(alex))

        selector.valid(true) shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("You must select at least 2 players for a match.")
    }

    @Test
    fun `Should always be valid for 2, 3 or 4 players`()
    {
        val p1 = insertPlayer()
        val p2 = insertPlayer()

        val players = mutableListOf(p1, p2)
        while (players.size < 4)
        {
            val selector = PlayerSelector()
            selector.init(players)

            selector.valid(true) shouldBe true
            selector.valid(false) shouldBe true
            dialogFactory.errorsShown.shouldBeEmpty()

            val p = insertPlayer()
            players.add(p)
        }
    }

    @Test
    fun `Should always be invalid for 5 players`()
    {
        val players = listOf(insertPlayer(), insertPlayer(), insertPlayer(), insertPlayer(), insertPlayer())

        val selector = PlayerSelector()
        selector.init(players)

        selector.valid(true) shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("You cannot have more than 4 players.")

        dialogFactory.errorsShown.clear()
        selector.valid(false) shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("You cannot have more than 4 players.")
    }
}