package dartzee.screen.game

import dartzee.core.util.MathsUtil
import dartzee.`object`.Dart
import dartzee.utils.getLongestStreak
import java.util.stream.IntStream

open class GameStatisticsPanelRoundTheClock : GameStatisticsPanel()
{
    override fun addRowsToTable()
    {
        addRow(getDartsPerNumber({ i -> getMaxDartsForAnyRound(i) }, "Most darts", true))
        addRow(getDartsPerNumber({ i -> getAverageDartsForAnyRound(i) }, "Avg. darts", false))
        addRow(getDartsPerNumber({ i -> getMinDartsForAnyRound(i) }, "Fewest darts", false))

        //addRow(arrayOfNulls(getRowWidth()))

        addRow(getLongestStreak())
        addRow(getBruceys("Brucey chances", false))
        addRow(getBruceys("Bruceys executed", true))

        //addRow(arrayOfNulls(getRowWidth()))

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

    private fun getMaxDartsForAnyRound(darts: IntStream): Any
    {
        //This includes incomplete rounds, so there'll always be something.#
        return darts.max().asInt
    }

    private fun getAverageDartsForAnyRound(darts: IntStream): Any
    {
        val oi = darts.average()
        return if (!oi.isPresent)
        {
            "N/A"
        } else MathsUtil.round(oi.asDouble, 2)

    }

    private fun getMinDartsForAnyRound(darts: IntStream): Any
    {
        val oi = darts.min()
        return if (!oi.isPresent)
        {
            "N/A"
        } else oi.asInt

    }

    private fun getBruceys(desc: String, enforceSuccess: Boolean): Array<Any?>
    {
        val row = factoryRow(desc)
        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]

            val rounds = hmPlayerToDarts[playerName]!!
            var bruceyRounds = rounds.filter { r -> r.size == 4 }

            if (enforceSuccess)
            {
                bruceyRounds = bruceyRounds.filter { r -> r.last().hitClockTarget(gameParams) }
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
            row[i + 1] = dartsGrouped.stream()
                    .mapToInt { g -> g.size }
                    .filter { ix -> ix in min..max }
                    .count()
        }

        return row
    }

    private fun getDartsPerNumber(fn: (stream: IntStream) -> Any, desc: String, includeUnfinished: Boolean): Array<Any?>
    {
        val row = factoryRow(desc)
        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]

            var dartsGrouped = getDartsGroupedByParticipantAndNumber(playerName)
            if (!includeUnfinished)
            {
                dartsGrouped = dartsGrouped.filter { g -> g.last().hitClockTarget(gameParams) }.toMutableList()
            }

            val stream = dartsGrouped.stream().mapToInt { g -> g.size }
            row[i + 1] = fn.invoke(stream)
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
