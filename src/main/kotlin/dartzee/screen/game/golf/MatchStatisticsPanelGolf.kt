package dartzee.screen.game.golf

import dartzee.core.util.minOrZero

class MatchStatisticsPanelGolf: GameStatisticsPanelGolf()
{
    override fun addRowsToTable()
    {
        super.addRowsToTable()

        addRow(getBestGameRow { it.minOrZero() })
        addRow(getAverageGameRow())
    }

    override fun getRankedRowsLowestWins() = super.getRankedRowsLowestWins() + "Best Game" + "Avg Game"
}
