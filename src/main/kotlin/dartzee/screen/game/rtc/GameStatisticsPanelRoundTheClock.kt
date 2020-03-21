package dartzee.screen.game.rtc

import dartzee.`object`.Dart
import dartzee.core.util.MathsUtil
import dartzee.core.util.maxOrZero
import dartzee.core.util.minOrZero
import dartzee.game.state.DefaultPlayerState
import dartzee.screen.game.AbstractGameStatisticsPanel
import dartzee.screen.game.scorer.DartsScorerRoundTheClock
import dartzee.utils.getLongestStreak

open class GameStatisticsPanelRoundTheClock(gameParams: String): AbstractGameStatisticsPanel<DefaultPlayerState<DartsScorerRoundTheClock>>(gameParams)
{
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

    private fun getLongestStreak(): Array<Any?>
    {
        val row = factoryRow("Best Streak")
        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]

            val darts = getFlattenedDarts(playerName)
            row[i + 1] = getLongestStreak(darts, gameParams!!).size
        }

        return row
    }

    private fun getAverageDartsForAnyRound(darts: List<Int>): Any
    {
        val oi = darts.average()
        return if (oi == 0.0)
        {
            "N/A"
        } else MathsUtil.round(oi, 2)

    }

    private fun getBruceys(desc: String, enforceSuccess: Boolean): Array<Any?>
    {
        val row = factoryRow(desc)
        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]

            val rounds = hmPlayerToDarts[playerName]!!
            var bruceyRounds = rounds.filter { it.size == 4 }

            if (enforceSuccess)
            {
                bruceyRounds = bruceyRounds.filter { it.last().hitClockTarget(gameParams) }
            }

            row[i + 1] = bruceyRounds.size
        }

        return row
    }

    private fun getDartsPerNumber(min: Int, max: Int, desc: String = "$min - $max"): Array<Any?>
    {
        val row = factoryRow(desc)
        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]

            val dartsGrouped = getDartsGroupedByParticipantAndNumber(playerName)
            row[i + 1] = dartsGrouped.filter { it.size in min..max }.size
        }

        return row
    }

    private fun getDartsPerNumber(desc: String, includeUnfinished: Boolean, fn: (stream: List<Int>) -> Any): Array<Any?>
    {
        val row = factoryRow(desc)
        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]

            var dartsGrouped = getDartsGroupedByParticipantAndNumber(playerName)
            if (!includeUnfinished)
            {
                dartsGrouped = dartsGrouped.filter { it.last().hitClockTarget(gameParams) }.toMutableList()
            }

            val sizes = dartsGrouped.map { it.size }
            row[i + 1] = fn.invoke(sizes)
        }

        return row
    }

    private fun getDartsGroupedByParticipantAndNumber(playerName: String): MutableList<List<Dart>>
    {
        val darts = getFlattenedDarts(playerName)
        val hm = darts.groupBy{d -> "${d.participantId}_${d.startingScore}"}
        return hm.values.toMutableList()
    }

    override fun getRankedRowsHighestWins(): MutableList<String>
    {
        return mutableListOf("Best Streak", "Brucey chances", "Bruceys executed")
    }

    override fun getRankedRowsLowestWins(): MutableList<String>
    {
        return mutableListOf("Most darts", "Avg. darts", "Fewest darts")
    }

    override fun getHistogramRows(): MutableList<String>
    {
        return mutableListOf("1", "2 - 3", "4 - 6", "6 - 10", "10 - 15", "16 - 20", "21+")
    }

    override fun getStartOfSectionRows(): MutableList<String>
    {
        return mutableListOf("Best Streak", "1", "Best Game")
    }
}
