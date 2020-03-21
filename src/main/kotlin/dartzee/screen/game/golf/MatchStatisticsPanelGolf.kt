package dartzee.screen.game.golf

import dartzee.core.util.minOrZero

class MatchStatisticsPanelGolf(gameParams: String): GameStatisticsPanelGolf(gameParams)
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
