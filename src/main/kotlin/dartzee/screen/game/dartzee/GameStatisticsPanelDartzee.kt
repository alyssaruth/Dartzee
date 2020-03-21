package dartzee.screen.game.dartzee

import dartzee.core.util.MathsUtil
import dartzee.core.util.maxOrZero
import dartzee.core.util.minOrZero
import dartzee.db.DartzeeRoundResultEntity
import dartzee.game.state.DartzeePlayerState
import dartzee.screen.game.AbstractGameStatisticsPanel

open class GameStatisticsPanelDartzee(gameParams: String): AbstractGameStatisticsPanel<DartzeePlayerState>(gameParams)
{
    override fun addRowsToTable()
    {
        addRow(getScoreRow("Highest Score") { it.maxOrZero() })
        addRow(getScoreRow("Avg. Score") { MathsUtil.round(it.average(), 1) })
        addRow(getScoreRow("Lowest Score") { it.minOrZero() })
    }

    override fun getRankedRowsHighestWins(): List<String> {
        return mutableListOf("Highest Score", "Avg Score", "Lowest Score")
    }

    override fun getRankedRowsLowestWins(): List<String> {
        return mutableListOf()
    }

    override fun getHistogramRows(): List<String> {
        return mutableListOf()
    }

    override fun getStartOfSectionRows(): List<String> {
        return mutableListOf()
    }

    private fun getScoreRow(desc: String, f: (i: List<Int>) -> Number): Array<Any?>
    {
        return prepareRow(desc) { playerName ->
            val results = getRoundResults(playerName)
            val scores = results.map { it.score }

            if (scores.isEmpty()) null else f(scores)
        }
    }

    private fun getRoundResults(playerName: String): List<DartzeeRoundResultEntity>
    {
        val states = hmPlayerToStates[playerName]
        return states?.flatMap { it.roundResults } ?: listOf()
    }
}