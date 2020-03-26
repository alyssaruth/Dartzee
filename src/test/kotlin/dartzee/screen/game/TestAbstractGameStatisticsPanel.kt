package dartzee.screen.game

import arrow.core.left
import dartzee.game.state.DefaultPlayerState
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.makeDart
import dartzee.helper.makeDefaultPlayerState
import dartzee.utils.DartsColour
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color
import java.awt.Component
import javax.swing.JComponent
import javax.swing.border.MatteBorder


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


}

class TestAbstractGameStatisticsPanelRenderers: AbstractTest()
{
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

    @Test
    fun `Should colour highest wins rows correctly when there is a tie`()
    {
        val state1 = makeDefaultPlayerState(insertPlayer(name = "Alice"), dartsThrown = listOf(makeDart(), makeDart(), makeDart()))
        val state2 = makeDefaultPlayerState(insertPlayer(name = "Bob"), dartsThrown = listOf(makeDart()))
        val state3 = makeDefaultPlayerState(insertPlayer(name = "Clive"), dartsThrown = listOf(makeDart(), makeDart(), makeDart()))
        val state4 = makeDefaultPlayerState(insertPlayer(name = "Derek"), dartsThrown = listOf(makeDart(), makeDart(), makeDart(), makeDart()))

        val panel = FakeGameStatisticsPanel(highestWins = listOf("Darts Thrown"))
        panel.showStats(listOf(state1, state2, state3, state4))

        panel.getCellComponent(0, 1).shouldHaveColours(DartsColour.SECOND_COLOURS)
        panel.getCellComponent(0, 2).shouldHaveColours(DartsColour.FOURTH_COLOURS)
        panel.getCellComponent(0, 3).shouldHaveColours(DartsColour.SECOND_COLOURS)
        panel.getCellComponent(0, 4).shouldHaveColours(DartsColour.FIRST_COLOURS)
    }

    @Test
    fun `Should set correct edge borders`()
    {
        val state1 = makeDefaultPlayerState(insertPlayer(name = "Alice"), dartsThrown = listOf(makeDart(), makeDart(), makeDart()))
        val state2 = makeDefaultPlayerState(insertPlayer(name = "Bob"), dartsThrown = listOf(makeDart()))
        val state3 = makeDefaultPlayerState(insertPlayer(name = "Clive"), dartsThrown = listOf(makeDart(), makeDart(), makeDart()))
        val state4 = makeDefaultPlayerState(insertPlayer(name = "Derek"), dartsThrown = listOf(makeDart(), makeDart(), makeDart(), makeDart()))

        val panel = FakeGameStatisticsPanel()
        panel.showStats(listOf(state1, state2, state3, state4))

        panel.getCellComponent(0, 0).shouldHaveBorderThickness(2, 1, 0, 2)
        panel.getCellComponent(0, 1).shouldHaveBorderThickness(1, 1, 0, 2)
        panel.getCellComponent(0, 2).shouldHaveBorderThickness(1, 1, 0, 2)
        panel.getCellComponent(0, 3).shouldHaveBorderThickness(1, 1, 0, 2)
        panel.getCellComponent(0, 4).shouldHaveBorderThickness(1, 2, 0, 2)
    }

    @Test
    fun `Should additionally set top borders if row is end of section`()
    {
        val state1 = makeDefaultPlayerState(insertPlayer(name = "Alice"), dartsThrown = listOf(makeDart(), makeDart(), makeDart()))
        val state2 = makeDefaultPlayerState(insertPlayer(name = "Bob"), dartsThrown = listOf(makeDart()))
        val state3 = makeDefaultPlayerState(insertPlayer(name = "Clive"), dartsThrown = listOf(makeDart(), makeDart(), makeDart()))
        val state4 = makeDefaultPlayerState(insertPlayer(name = "Derek"), dartsThrown = listOf(makeDart(), makeDart(), makeDart(), makeDart()))

        val panel = FakeGameStatisticsPanel(sectionStarts = listOf("Darts Thrown"))
        panel.showStats(listOf(state1, state2, state3, state4))

        panel.getCellComponent(0, 0).shouldHaveBorderThickness(2, 1, 2, 2)
        panel.getCellComponent(0, 1).shouldHaveBorderThickness(1, 1, 2, 2)
        panel.getCellComponent(0, 2).shouldHaveBorderThickness(1, 1, 2, 2)
        panel.getCellComponent(0, 3).shouldHaveBorderThickness(1, 1, 2, 2)
        panel.getCellComponent(0, 4).shouldHaveBorderThickness(1, 2, 2, 2)
    }

    @Test
    fun `Should render the title column correctly`()
    {
        val state = makeDefaultPlayerState(insertPlayer(name = "Alice"), dartsThrown = listOf(makeDart(), makeDart(), makeDart()))

        val panel = FakeGameStatisticsPanel(sectionStarts = listOf("Darts Thrown"))
        panel.showStats(listOf(state))

        panel.getCellComponent(0, 0).shouldHaveColours(Pair(Color.WHITE, null))
        panel.getCellComponent(0, 0).font.isBold shouldBe true
    }
}

private fun Component.shouldHaveColours(colours: Pair<Color?, Color?>)
{
    background shouldBe colours.first
    foreground shouldBe colours.second
}

private fun JComponent.shouldHaveBorderThickness(left: Int, right: Int, top: Int, bottom: Int)
{
    val insets = border.getBorderInsets(this)
    insets.left shouldBe left
    insets.right shouldBe right
    insets.top shouldBe top
    insets.bottom shouldBe bottom
}


private class FakeGameStatisticsPanel(private val highestWins: List<String> = emptyList(),
                                      private val lowestWins: List<String> = emptyList(),
                                      private val histograms: List<String> = emptyList(),
                                      private val sectionStarts: List<String> = emptyList()): AbstractGameStatisticsPanel<DefaultPlayerState<*>>("")
{
    override fun getRankedRowsHighestWins() = highestWins
    override fun getRankedRowsLowestWins() = lowestWins
    override fun getHistogramRows() = histograms
    override fun getStartOfSectionRows() = sectionStarts

    override fun addRowsToTable() {
        addRow(getDartsThrownRow())
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