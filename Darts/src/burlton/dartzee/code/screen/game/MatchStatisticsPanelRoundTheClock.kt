package burlton.dartzee.code.screen.game

class MatchStatisticsPanelRoundTheClock : GameStatisticsPanelRoundTheClock()
{
    override fun addRowsToTable()
    {
        super.addRowsToTable()

        addRow(arrayOfNulls(getRowWidth()))
        addRow(getBestGameRow { stream -> stream.min() })
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
