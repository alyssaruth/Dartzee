package dartzee.screen.game

import dartzee.core.util.minOrZero
import dartzee.utils.isFinishRound
import dartzee.utils.sumScore

class MatchStatisticsPanelX01 : GameStatisticsPanelX01()
{
    override fun addRowsToTable()
    {
        super.addRowsToTable()

        addRow(getHighestFinishRow())

        addRow(getBestGameRow { it.minOrZero() })
        addRow(getAverageGameRow())
    }

    private fun getHighestFinishRow(): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = "Best Finish"

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val rounds = hmPlayerToDarts[playerName]!!

            val finishRounds = rounds.filter { r -> isFinishRound(r) }
            row[i + 1] = finishRounds.map { r -> sumScore(r) }.max() ?: "N/A"
        }

        return row
    }

    override fun getRankedRowsHighestWins(): MutableList<String>
    {
        val v = super.getRankedRowsHighestWins()
        v.add("Best Finish")
        return v
    }

    override fun getRankedRowsLowestWins(): MutableList<String>
    {
        val v = super.getRankedRowsLowestWins()
        v.add("Best Game")
        v.add("Avg Game")
        return v
    }
}
