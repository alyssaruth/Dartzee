package dartzee.screen.game.dartzee

import dartzee.core.util.MathsUtil
import dartzee.core.util.getLongestStreak
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

        addRow(getLongestStreakRow())
    }

    override fun getRankedRowsHighestWins() = listOf("Highest Score", "Avg Score", "Lowest Score", "Longest Streak")
    override fun getRankedRowsLowestWins() = emptyList<String>()
    override fun getHistogramRows() = emptyList<String>()
    override fun getStartOfSectionRows() = listOf("Longest Streak")

    private fun getScoreRow(desc: String, f: (i: List<Int>) -> Number) = prepareRow(desc) { playerName ->
        val results = getRoundResults(playerName).flatten()
        val scores = results.map { it.score }

        if (scores.isEmpty()) null else f(scores)
    }

    private fun getLongestStreakRow() = prepareRow("Longest Streak") { playerName ->
        val allResults = getRoundResults(playerName)

        allResults.map { results -> results.getLongestStreak { it.success }.size }.max()
    }

    private fun getRoundResults(playerName: String): List<List<DartzeeRoundResultEntity>>
    {
        val states = hmPlayerToStates[playerName]
        return states?.map { it.roundResults } ?: listOf()
    }
}