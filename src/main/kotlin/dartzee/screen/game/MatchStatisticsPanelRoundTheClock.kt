package dartzee.screen.game

import dartzee.core.util.minOrZero

class MatchStatisticsPanelRoundTheClock : GameStatisticsPanelRoundTheClock()
{
    override fun addRowsToTable()
    {
        super.addRowsToTable()

        //addRow(arrayOfNulls(getRowWidth()))
        addRow(getBestGameRow { stream -> stream.minOrZero() })
        addRow(getAverageGameRow())
    }

    override fun getRankedRowsLowestWins(): MutableList<String>
    {
        val ret = super.getRankedRowsLowestWins()
        ret.add("Best Game")
        ret.add("Avg Game")
        return ret
    }
}
