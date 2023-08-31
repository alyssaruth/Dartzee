package dartzee.screen.game.x01

import dartzee.core.util.minOrZero
import dartzee.utils.isFinishRound
import dartzee.utils.sumScore

class MatchStatisticsPanelX01(gameParams: String): GameStatisticsPanelX01(gameParams)
{
    override fun addRowsToTable()
    {
        super.addRowsToTable()

        addRow(getHighestFinishRow())

        addRow(getBestGameRow { it.minOrZero() })
        addRow(getAverageGameRow())
    }

    private fun getHighestFinishRow() = prepareRow("Best Finish") { playerName ->
        val rounds = hmPlayerToDarts[playerName].orEmpty()
        val finishRounds = rounds.filter { r -> isFinishRound(r) }
        finishRounds.maxOfOrNull { r -> sumScore(r) }
    }

    override fun getRankedRowsHighestWins() = super.getRankedRowsHighestWins() + "Best Finish"
    override fun getRankedRowsLowestWins() = super.getRankedRowsLowestWins() + "Best Game" + "Avg Game"
}
