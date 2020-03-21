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

    override fun getStartOfSectionRows() = super.getStartOfSectionRows() + "Best Game"
    override fun getRankedRowsHighestWins() = super.getRankedRowsHighestWins() + "Best Game" + "Avg Game"
}