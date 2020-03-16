package dartzee.screen.game.golf

import dartzee.core.util.minOrZero
import dartzee.screen.game.golf.GameStatisticsPanelGolf

class MatchStatisticsPanelGolf : GameStatisticsPanelGolf()
{
    override fun addRowsToTable()
    {
        super.addRowsToTable()

        addRow(getBestGameRow { it.minOrZero() })
        addRow(getAverageGameRow())
    }

    override fun getRankedRowsLowestWins(): MutableList<String>
    {
        val rows = super.getRankedRowsLowestWins()
        rows.add("Best Game")
        rows.add("Avg Game")
        return rows
    }
}
