package dartzee.screen.game

import dartzee.core.util.maxOrZero
import dartzee.game.state.X01PlayerState
import dartzee.helper.*
import dartzee.shouldHaveColours
import dartzee.utils.DartsColour
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JComponent


class TestAbstractGameStatisticsPanel: AbstractTest()
{
    @Test
    fun `Should combine like participants and preserve original player order`()
    {
        val clive = insertPlayer(name = "Clive")
        val alice = insertPlayer(name = "Alice")

        val cliveState1 = makeX01PlayerState(player = clive, completedRound = listOf(makeDart(), makeDart(), makeDart()))
        val aliceState1 = makeX01PlayerState(player = alice, completedRound = listOf(makeDart()))
        val aliceState2 = makeX01PlayerState(player = alice, completedRound = listOf(makeDart(), makeDart()))
        val cliveState2 = makeX01PlayerState(player = clive, completedRound = listOf(makeDart(), makeDart(), makeDart(), makeDart(), makeDart()))

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
        val cliveState1 = makeX01PlayerState(player = clive, completedRound = listOf(makeDart(), makeDart(), makeDart()))

        val panel = FakeGameStatisticsPanel()
        panel.showStats(listOf(cliveState1))

        val cliveState2 = makeX01PlayerState(player = clive, completedRound = listOf(makeDart()))
        panel.showStats(listOf(cliveState2))

        panel.getValueForRow("Darts Thrown", 1) shouldBe 1
    }


    @Test
    fun `Should show nothing if there is insufficient data`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val aliceState = makeX01PlayerState(player = alice, completedRound = listOf(makeDart()))
        val bobState = makeX01PlayerState(player = bob, completedRound = listOf())

        val panel = FakeGameStatisticsPanel()
        panel.showStats(listOf(aliceState, bobState))

        panel.tm.rowCount shouldBe 0
    }

    @Test
    fun `Should calculate best game correctly`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val aliceState1 = makeX01PlayerState(player = alice, participant = insertParticipant(playerId = alice.rowId, finalScore = 50), completedRound = listOf(makeDart()))
        val aliceState2 = makeX01PlayerState(player = alice, participant = insertParticipant(playerId = alice.rowId, finalScore = 35), completedRound = listOf(makeDart()))

        val bobState1 = makeX01PlayerState(player = bob, participant = insertParticipant(playerId = bob.rowId, finalScore = 28), completedRound = listOf(makeDart()))
        val bobState2 = makeX01PlayerState(player = bob, participant = insertParticipant(playerId = bob.rowId, finalScore = 70), completedRound = listOf(makeDart()))
        val bobState3 = makeX01PlayerState(player = bob, participant = insertParticipant(playerId = bob.rowId, finalScore = -1), completedRound = listOf(makeDart()))

        val panel = FakeGameStatisticsPanel()
        panel.showStats(listOf(aliceState1, bobState1, bobState2, aliceState2, bobState3))

        panel.getValueForRow("Best Game", 1) shouldBe 50
        panel.getValueForRow("Best Game", 2) shouldBe 70
    }

    @Test
    fun `Should calculate avg game correctly`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val aliceState1 = makeX01PlayerState(player = alice, participant = insertParticipant(playerId = alice.rowId, finalScore = 50), completedRound = listOf(makeDart()))
        val aliceState2 = makeX01PlayerState(player = alice, participant = insertParticipant(playerId = alice.rowId, finalScore = 35), completedRound = listOf(makeDart()))

        val bobState1 = makeX01PlayerState(player = bob, participant = insertParticipant(playerId = bob.rowId, finalScore = 28), completedRound = listOf(makeDart()))
        val bobState2 = makeX01PlayerState(player = bob, participant = insertParticipant(playerId = bob.rowId, finalScore = 70), completedRound = listOf(makeDart()))

        val panel = FakeGameStatisticsPanel()
        panel.showStats(listOf(aliceState1, bobState1, bobState2, aliceState2))

        panel.getValueForRow("Avg Game", 1) shouldBe 42.5
        panel.getValueForRow("Avg Game", 2) shouldBe 49.0
    }

    @Test
    fun `Should replace NULL values with NA`()
    {
        val state = makeX01PlayerState(completedRound = listOf(makeDart()))

        val panel = FakeGameStatisticsPanel()
        panel.showStats(listOf(state))

        panel.getValueForRow("Nulls", 1) shouldBe "N/A"
    }

    /**
     * Couple of renderer tests just to prove it's being set on all appropriate columns
     */
    @Test
    fun `Should colour highest wins rows correctly`()
    {
        val state1 = makeX01PlayerState(player = insertPlayer(name = "Alice"), completedRound = listOf(makeDart(), makeDart(), makeDart()))
        val state2 = makeX01PlayerState(player = insertPlayer(name = "Bob"), completedRound = listOf(makeDart()))
        val state3 = makeX01PlayerState(player = insertPlayer(name = "Clive"), completedRound = listOf(makeDart(), makeDart()))
        val state4 = makeX01PlayerState(player = insertPlayer(name = "Derek"), completedRound = listOf(makeDart(), makeDart(), makeDart(), makeDart()))

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
        val state1 = makeX01PlayerState(player = insertPlayer(name = "Alice"), completedRound = listOf(makeDart(), makeDart(), makeDart()))
        val state2 = makeX01PlayerState(player = insertPlayer(name = "Bob"), completedRound = listOf(makeDart()))
        val state3 = makeX01PlayerState(player = insertPlayer(name = "Clive"), completedRound = listOf(makeDart(), makeDart()))
        val state4 = makeX01PlayerState(player = insertPlayer(name = "Derek"), completedRound = listOf(makeDart(), makeDart(), makeDart(), makeDart()))

        val panel = FakeGameStatisticsPanel(lowestWins = listOf("Darts Thrown"))
        panel.showStats(listOf(state1, state2, state3, state4))

        panel.getCellComponent(0, 1).shouldHaveColours(DartsColour.THIRD_COLOURS)
        panel.getCellComponent(0, 2).shouldHaveColours(DartsColour.FIRST_COLOURS)
        panel.getCellComponent(0, 3).shouldHaveColours(DartsColour.SECOND_COLOURS)
        panel.getCellComponent(0, 4).shouldHaveColours(DartsColour.FOURTH_COLOURS)
    }
}

private class FakeGameStatisticsPanel(private val highestWins: List<String> = emptyList(),
                                      private val lowestWins: List<String> = emptyList()): AbstractGameStatisticsPanel<X01PlayerState>()
{
    override fun getRankedRowsHighestWins() = highestWins
    override fun getRankedRowsLowestWins() = lowestWins
    override fun getHistogramRows() = emptyList<String>()
    override fun getStartOfSectionRows() = emptyList<String>()

    override fun addRowsToTable() {
        addRow(getDartsThrownRow())
        addRow(getBestGameRow() { it.maxOrZero() })
        addRow(getAverageGameRow())
        addRow(getNullRow())
    }

    private fun getDartsThrownRow() = prepareRow("Darts Thrown") { playerName ->
        val darts = hmPlayerToDarts[playerName] ?: emptyList()
        darts.map { it.size }.sum()
    }

    private fun getNullRow() = prepareRow("Nulls") { null }

    fun getCellComponent(row: Int, column: Int): JComponent
    {
        val renderer = table.getColumn(column).cellRenderer
        return renderer.getTableCellRendererComponent(table.table, null, false, false, row, column) as JComponent
    }

}