package dartzee.screen.game.dartzee

import dartzee.core.util.maxOrZero

class MatchStatisticsPanelDartzee(gameParams: String): GameStatisticsPanelDartzee(gameParams)
{
    override fun addRowsToTable()
    {
        super.addRowsToTable()

        addRow(getBestGameRow { it.maxOrZero() })
        addRow(getAverageGameRow())
    }

    override fun getRankedRowsHighestWins(): MutableList<String>
    {
        val rows = super.getRankedRowsHighestWins()
        rows.add("Best Game")
        rows.add("Avg Game")
        return rows
    }
}