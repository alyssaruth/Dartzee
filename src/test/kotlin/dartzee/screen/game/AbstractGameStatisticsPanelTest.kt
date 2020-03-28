package dartzee.screen.game

import dartzee.core.util.Debug
import dartzee.game.state.AbstractPlayerState
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldContainAll
import org.junit.Test

abstract class AbstractGameStatisticsPanelTest<PlayerState: AbstractPlayerState<*>, S: AbstractGameStatisticsPanel<PlayerState>>: AbstractTest()
{
    abstract fun factoryStatsPanel(): S
    abstract fun makePlayerState(): PlayerState

    @Test
    fun `Categorised rows should all exist in the table model`()
    {
        val statsPanel = factoryStatsPanel()
        statsPanel.showStats(listOf(makePlayerState()))

        val rowNames = statsPanel.getAllRowNames()
        val categorisedRows = statsPanel.getRankedRowsHighestWins() + statsPanel.getRankedRowsLowestWins() + statsPanel.getHistogramRows()

        rowNames.shouldContainAll(categorisedRows)
    }
}

private fun AbstractGameStatisticsPanel<*>.getAllRowNames(): List<String>
{
    val rows = 0 until tm.rowCount
    return rows.map { tm.getValueAt(it, 0) as String }
}

fun AbstractGameStatisticsPanel<*>.getValueForRow(rowName: String, column: Int = 1): Any?
{
    val rowIndex = getAllRowNames().indexOf(rowName)
    if (rowIndex > -1)
    {
        return tm.getValueAt(rowIndex, column)
    }

    Debug.stackTrace("No row called $rowName")
    return null
}