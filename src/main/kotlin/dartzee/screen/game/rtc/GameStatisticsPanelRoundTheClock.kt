package dartzee.screen.game.rtc

import dartzee.`object`.Dart
import dartzee.core.util.MathsUtil
import dartzee.core.util.maxOrZero
import dartzee.core.util.minOrZero
import dartzee.game.state.DefaultPlayerState
import dartzee.screen.game.AbstractGameStatisticsPanel
import dartzee.screen.game.scorer.DartsScorerRoundTheClock
import dartzee.utils.getLongestStreak

open class GameStatisticsPanelRoundTheClock(private val gameParams: String): AbstractGameStatisticsPanel<DefaultPlayerState<DartsScorerRoundTheClock>>()
{
    override fun getRankedRowsHighestWins() = listOf("Best Streak", "Brucey chances", "Bruceys executed")
    override fun getRankedRowsLowestWins() = listOf("Most darts", "Avg. darts", "Fewest darts")
    override fun getHistogramRows() = listOf("1", "2 - 3", "4 - 6", "6 - 10", "10 - 15", "16 - 20", "21+")
    override fun getStartOfSectionRows() = listOf("Best Streak", "1", "Best Game")

    override fun addRowsToTable()
    {
        addRow(getDartsPerNumber("Most darts", true) { it.maxOrZero() })
        addRow(getDartsPerNumber("Avg. darts", false) { getAverageDartsForAnyRound(it) })
        addRow(getDartsPerNumber("Fewest darts", false) { it.minOrZero() })

        addRow(getLongestStreak())
        addRow(getBruceys("Brucey chances", false))
        addRow(getBruceys("Bruceys executed", true))

        addRow(getDartsPerNumber(1, 1, "1"))
        addRow(getDartsPerNumber(2, 3))
        addRow(getDartsPerNumber(4, 6))
        addRow(getDartsPerNumber(6, 10))
        addRow(getDartsPerNumber(10, 15))
        addRow(getDartsPerNumber(16, 20))
        addRow(getDartsPerNumber(21, Integer.MAX_VALUE, "21+"))

        table.setColumnWidths("140")
    }

    private fun getLongestStreak() = prepareRow("Best Streak") { playerName ->
        val darts = getFlattenedDarts(playerName)
        getLongestStreak(darts, gameParams).size
    }

    private fun getAverageDartsForAnyRound(darts: List<Int>): Any
    {
        val oi = darts.average()
        return if (oi == 0.0) "N/A" else MathsUtil.round(oi, 2)

    }

    private fun getBruceys(desc: String, enforceSuccess: Boolean) = prepareRow(desc) { playerName ->
        val rounds = hmPlayerToDarts[playerName] ?: listOf()
        rounds.filter { it.size == 4 }.count { it.last().hitClockTarget(gameParams) || !enforceSuccess }
    }

    private fun getDartsPerNumber(min: Int, max: Int, desc: String = "$min - $max") = prepareRow(desc) { playerName ->
        val dartsGrouped = getDartsGroupedByParticipantAndNumber(playerName)
        dartsGrouped.count { it.size in min..max }
    }

    private fun getDartsPerNumber(desc: String,
                                  includeUnfinished: Boolean,
                                  fn: (stream: List<Int>) -> Any) = prepareRow(desc) { playerName ->
        val dartsGrouped = getDartsGroupedByParticipantAndNumber(playerName)
        val sizes = dartsGrouped.filter { it.last().hitClockTarget(gameParams) || includeUnfinished }.map { it.size }
        fn(sizes)
    }

    private fun getDartsGroupedByParticipantAndNumber(playerName: String): List<List<Dart>>
    {
        val darts = getFlattenedDarts(playerName)
        val hm = darts.groupBy{d -> "${d.participantId}_${d.startingScore}"}
        return hm.values.toList()
    }
}
