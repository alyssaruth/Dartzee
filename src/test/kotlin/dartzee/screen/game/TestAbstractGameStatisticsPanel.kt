package dartzee.screen.game

import dartzee.game.state.DefaultPlayerState
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.makeDart
import dartzee.helper.makeDefaultPlayerState
import io.kotlintest.shouldBe
import org.junit.Test


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

}