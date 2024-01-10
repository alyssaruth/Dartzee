package dartzee.screen.game.rtc

import dartzee.core.util.MathsUtil
import dartzee.core.util.maxOrZero
import dartzee.game.RoundTheClockConfig
import dartzee.game.UniqueParticipantName
import dartzee.game.state.ClockPlayerState
import dartzee.`object`.Dart
import dartzee.screen.game.AbstractGameStatisticsPanel
import dartzee.utils.getLongestStreak

open class GameStatisticsPanelRoundTheClock(gameParams: String) :
    AbstractGameStatisticsPanel<ClockPlayerState>() {
    private val config = RoundTheClockConfig.fromJson(gameParams)

    override fun getRankedRowsHighestWins() =
        listOf("Best Streak", "Brucey chances", "Bruceys executed")

    override fun getRankedRowsLowestWins() = listOf("Most darts", "Avg darts", "Fewest darts")

    override fun getHistogramRows() =
        listOf("1", "2 - 3", "4 - 6", "7 - 10", "11 - 15", "16 - 20", "21+")

    override fun getStartOfSectionRows() = listOf("Best Streak", "1", "Best Game")

    override fun addRowsToTable() {
        addRow(getDartsPerNumber("Most darts", true) { it.maxOrZero() })
        addRow(getDartsPerNumber("Avg darts", false) { getAverageDartsForAnyRound(it) })
        addRow(getDartsPerNumber("Fewest darts", false) { it.minOrNull() })

        addRow(getLongestStreak())
        addRow(getBruceys("Brucey chances", false))
        addRow(getBruceys("Bruceys executed", true))

        addRow(getDartsPerNumber(1, 1, "1"))
        addRow(getDartsPerNumber(2, 3))
        addRow(getDartsPerNumber(4, 6))
        addRow(getDartsPerNumber(7, 10))
        addRow(getDartsPerNumber(11, 15))
        addRow(getDartsPerNumber(16, 20))
        addRow(getDartsPerNumber(21, Integer.MAX_VALUE, "21+"))

        table.setColumnWidths("140")
    }

    private fun getLongestStreak() =
        prepareRow("Best Streak") { playerName ->
            val darts = getFlattenedDarts(playerName)
            getLongestStreak(darts, config.clockType).size
        }

    private fun getAverageDartsForAnyRound(darts: List<Int>) =
        if (darts.isEmpty()) null else MathsUtil.round(darts.average(), 2)

    private fun getBruceys(desc: String, enforceSuccess: Boolean) =
        prepareRow(desc) { playerName ->
            val rounds = hmPlayerToDarts[playerName].orEmpty()
            rounds.count {
                it.size == 4 && (it.last().hitClockTarget(config.clockType) || !enforceSuccess)
            }
        }

    private fun getDartsPerNumber(min: Int, max: Int, desc: String = "$min - $max") =
        prepareRow(desc) { playerName ->
            val dartsGrouped = getDartsGroupedByParticipantAndNumber(playerName)
            dartsGrouped.count { it.size in min..max && it.last().hitClockTarget(config.clockType) }
        }

    private fun getDartsPerNumber(
        desc: String,
        includeUnfinished: Boolean,
        fn: (stream: List<Int>) -> Any?
    ) =
        prepareRow(desc) { playerName ->
            val dartsGrouped = getDartsGroupedByParticipantAndNumber(playerName)
            val sizes =
                dartsGrouped
                    .filter { it.last().hitClockTarget(config.clockType) || includeUnfinished }
                    .map { it.size }
            fn(sizes)
        }

    private fun getDartsGroupedByParticipantAndNumber(
        playerName: UniqueParticipantName
    ): List<List<Dart>> {
        val darts = getFlattenedDarts(playerName)
        val hm = darts.groupBy { d -> "${d.participantId}_${d.startingScore}" }
        return hm.values.toList()
    }
}
