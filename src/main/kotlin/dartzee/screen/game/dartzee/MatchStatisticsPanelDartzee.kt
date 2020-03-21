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

    override fun getStartOfSectionRows(): List<String>
    {
        val list = super.getStartOfSectionRows()
        return list + "Best Game"
    }

    override fun getRankedRowsHighestWins(): List<String>
    {
        val rows = super.getRankedRowsHighestWins()
        return rows + "Best Game" + "Avg Game"
    }
}