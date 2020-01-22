package dartzee.screen.game

class MatchStatisticsPanelGolf : GameStatisticsPanelGolf()
{
    override fun addRowsToTable()
    {
        super.addRowsToTable()

        //addRow(arrayOfNulls(getRowWidth()))

        addRow(getBestGameRow { s -> s.min() })
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
