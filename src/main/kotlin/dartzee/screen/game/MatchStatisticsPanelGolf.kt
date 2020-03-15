package dartzee.screen.game

import dartzee.core.util.minOrZero

class MatchStatisticsPanelGolf : GameStatisticsPanelGolf()
{
    override fun addRowsToTable()
    {
        super.addRowsToTable()

        addRow(getBestGameRow { s -> s.minOrZero() })
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
