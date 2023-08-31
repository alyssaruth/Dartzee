package dartzee.screen.game.dartzee

import dartzee.core.util.MathsUtil
import dartzee.core.util.getLongestStreak
import dartzee.core.util.maxOrZero
import dartzee.core.util.minOrZero
import dartzee.db.DartzeeRoundResultEntity
import dartzee.game.UniqueParticipantName
import dartzee.game.state.DartzeePlayerState
import dartzee.screen.game.AbstractGameStatisticsPanel

open class GameStatisticsPanelDartzee: AbstractGameStatisticsPanel<DartzeePlayerState>()
{
    override fun addRowsToTable()
    {
        addRow(getPeakScoreRow())
        addRow(getScoreRow("Highest Round") { it.maxOrZero() })
        addRow(getScoreRow("Avg Round") { MathsUtil.round(it.average(), 1) })
        addRow(getScoreRow("Lowest Round") { it.minOrZero() })

        addRow(getLongestStreakRow())
        addRow(getHardestRuleRow())
    }

    override fun getRankedRowsHighestWins() = listOf("Highest Round", "Avg Round", "Lowest Round", "Longest Streak", "Hardest Rule", "Peak Score")
    override fun getRankedRowsLowestWins() = emptyList<String>()
    override fun getHistogramRows() = emptyList<String>()
    override fun getStartOfSectionRows() = listOf("Longest Streak")

    private fun getPeakScoreRow() = prepareRow("Peak Score", ::getPeakScore)
    private fun getPeakScore(playerName: UniqueParticipantName): Any?
    {
        val states = hmPlayerToStates[playerName] ?: return null
        return states.mapNotNull { it.getPeakScore() }.maxOrNull()
    }

    private fun getScoreRow(desc: String, f: (i: List<Int>) -> Number) = prepareRow(desc) { playerName ->
        val results = getRoundResults(playerName).flatten()
        val scores = results.map { it.score }

        if (scores.isEmpty()) null else f(scores)
    }

    private fun getLongestStreakRow() = prepareRow("Longest Streak") { playerName ->
        val allResults = getRoundResults(playerName)

        allResults.maxOfOrNull { results -> results.getLongestStreak { it.success }.size }
    }

    private fun getHardestRuleRow() = prepareRow("Hardest Rule") { playerName ->
        val allResults = getRoundResults(playerName).flatten()
        allResults.filter { it.success }.maxOfOrNull { it.ruleNumber }
    }

    private fun getRoundResults(playerName: UniqueParticipantName): List<List<DartzeeRoundResultEntity>>
    {
        val states = hmPlayerToStates[playerName]
        return states?.map { it.roundResults }.orEmpty()
    }
}