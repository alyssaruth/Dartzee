package burlton.dartzee.code.screen.game

import burlton.dartzee.code.utils.isFinishRound
import burlton.dartzee.code.utils.sumScore

class MatchStatisticsPanelX01 : GameStatisticsPanelX01()
{
    override fun addRowsToTable()
    {
        super.addRowsToTable()

        addRow(getHighestFinishRow())

        //addRow(arrayOfNulls(getRowWidth()))

        addRow(getBestGameRow { s -> s.min() })
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
            if (finishRounds.isEmpty())
            {
                row[i + 1] = "N/A"
            }
            else
            {
                val stream = finishRounds.stream().mapToInt { r -> sumScore(r) }
                val max = stream.max().asInt

                row[i + 1] = max
            }
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
