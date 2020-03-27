package dartzee.screen.game

import dartzee.core.util.maxOrZero
import dartzee.game.state.DefaultPlayerState
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.makeDart
import dartzee.helper.makeDefaultPlayerState
import dartzee.shouldHaveColours
import dartzee.utils.DartsColour
import io.kotlintest.shouldBe
import org.junit.Test
import javax.swing.JComponent


class TestAbstractGameStatisticsPanel: AbstractTest()
{
    @Test
    fun `Should combine like participants and preserve original player order`()
    {
        val clive = insertPlayer(name = "Clive")
        val alice = insertPlayer(name = "Alice")

        val cliveState1 = makeDefaultPlayerState(clive, dartsThrown = listOf(makeDart(), makeDart(), makeDart()))
        val aliceState1 = makeDefaultPlayerState(alice, dartsThrown = listOf(makeDart()))
        val aliceState2 = makeDefaultPlayerState(alice, dartsThrown = listOf(makeDart(), makeDart()))
        val cliveState2 = makeDefaultPlayerState(clive, dartsThrown = listOf(makeDart(), makeDart(), makeDart(), makeDart(), makeDart()))

        val panel = FakeGameStatisticsPanel()
        panel.showStats(listOf(cliveState1, aliceState1, aliceState2, cliveState2))

        panel.tm.columnCount shouldBe 3
        panel.tm.getColumnName(0) shouldBe ""
        panel.tm.getColumnName(1) shouldBe "Clive"
        panel.tm.getColumnName(2) shouldBe "Alice"

        panel.getValueForRow("Darts Thrown", 1) shouldBe 8
        panel.getValueForRow("Darts Thrown", 2) shouldBe 3
    }

    @Test
    fun `Should clear down previous stats`()
    {
        val clive = insertPlayer(name = "Clive")
        val cliveState1 = makeDefaultPlayerState(clive, dartsThrown = listOf(makeDart(), makeDart(), makeDart()))

        val panel = FakeGameStatisticsPanel()
        panel.showStats(listOf(cliveState1))

        val cliveState2 = makeDefaultPlayerState(clive, dartsThrown = listOf(makeDart()))
        panel.showStats(listOf(cliveState2))

        panel.getValueForRow("Darts Thrown", 1) shouldBe 1
    }


    @Test
    fun `Should show nothing if there is insufficient data`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val aliceState = makeDefaultPlayerState(alice, dartsThrown = listOf(makeDart()))
        val bobState = makeDefaultPlayerState(bob, dartsThrown = listOf())

        val panel = FakeGameStatisticsPanel()
        panel.showStats(listOf(aliceState, bobState))

        panel.tm.rowCount shouldBe 0
    }

    /**
     * Couple of renderer tests just to prove it's being set on all appropriate columns
     */
    @Test
    fun `Should colour highest wins rows correctly`()
    {
        val state1 = makeDefaultPlayerState(insertPlayer(name = "Alice"), dartsThrown = listOf(makeDart(), makeDart(), makeDart()))
        val state2 = makeDefaultPlayerState(insertPlayer(name = "Bob"), dartsThrown = listOf(makeDart()))
        val state3 = makeDefaultPlayerState(insertPlayer(name = "Clive"), dartsThrown = listOf(makeDart(), makeDart()))
        val state4 = makeDefaultPlayerState(insertPlayer(name = "Derek"), dartsThrown = listOf(makeDart(), makeDart(), makeDart(), makeDart()))

        val panel = FakeGameStatisticsPanel(highestWins = listOf("Darts Thrown"))
        panel.showStats(listOf(state1, state2, state3, state4))

        panel.getCellComponent(0, 1).shouldHaveColours(DartsColour.SECOND_COLOURS)
        panel.getCellComponent(0, 2).shouldHaveColours(DartsColour.FOURTH_COLOURS)
        panel.getCellComponent(0, 3).shouldHaveColours(DartsColour.THIRD_COLOURS)
        panel.getCellComponent(0, 4).shouldHaveColours(DartsColour.FIRST_COLOURS)
    }

    @Test
    fun `Should colour lowest wins rows correctly`()
    {
        val state1 = makeDefaultPlayerState(insertPlayer(name = "Alice"), dartsThrown = listOf(makeDart(), makeDart(), makeDart()))
        val state2 = makeDefaultPlayerState(insertPlayer(name = "Bob"), dartsThrown = listOf(makeDart()))
        val state3 = makeDefaultPlayerState(insertPlayer(name = "Clive"), dartsThrown = listOf(makeDart(), makeDart()))
        val state4 = makeDefaultPlayerState(insertPlayer(name = "Derek"), dartsThrown = listOf(makeDart(), makeDart(), makeDart(), makeDart()))

        val panel = FakeGameStatisticsPanel(lowestWins = listOf("Darts Thrown"))
        panel.showStats(listOf(state1, state2, state3, state4))

        panel.getCellComponent(0, 1).shouldHaveColours(DartsColour.THIRD_COLOURS)
        panel.getCellComponent(0, 2).shouldHaveColours(DartsColour.FIRST_COLOURS)
        panel.getCellComponent(0, 3).shouldHaveColours(DartsColour.SECOND_COLOURS)
        panel.getCellComponent(0, 4).shouldHaveColours(DartsColour.FOURTH_COLOURS)
    }
}

private class FakeGameStatisticsPanel(private val highestWins: List<String> = emptyList(),
                                      private val lowestWins: List<String> = emptyList()): AbstractGameStatisticsPanel<DefaultPlayerState<*>>("")
{
    override fun getRankedRowsHighestWins() = highestWins
    override fun getRankedRowsLowestWins() = lowestWins
    override fun getHistogramRows() = emptyList<String>()
    override fun getStartOfSectionRows() = emptyList<String>()

    override fun addRowsToTable() {
        addRow(getDartsThrownRow())
        addRow(getBestGameRow() { it.maxOrZero() })
        addRow(getAverageGameRow())
    }

    private fun getDartsThrownRow() = prepareRow("Darts Thrown") { playerName ->
        val darts = hmPlayerToDarts[playerName] ?: emptyList()
        darts.map { it.size }.sum()
    }

    fun getCellComponent(row: Int, column: Int): JComponent
    {
        val renderer = table.getColumn(column).cellRenderer
        return renderer.getTableCellRendererComponent(table.table, null, false, false, row, column) as JComponent
    }

}