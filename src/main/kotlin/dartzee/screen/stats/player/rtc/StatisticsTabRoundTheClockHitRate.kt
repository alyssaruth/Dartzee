package dartzee.screen.stats.player.rtc

import dartzee.core.obj.HashMapCount
import dartzee.game.RoundTheClockConfig
import dartzee.screen.stats.player.AbstractStatisticsTabPieBreakdown
import dartzee.stats.GameWrapper
import java.awt.Color

class StatisticsTabRoundTheClockHitRate : AbstractStatisticsTabPieBreakdown()
{
    override val ranges = listOf(1..1, 2..2, 3..3, 4..6, 7..10, 11..15, 16..Int.MAX_VALUE)

    override fun getColorForRange(range: IntRange): Color
    {
        return when(range)
        {
            ranges[0] -> Color.GREEN
            ranges[1] -> Color.getHSBColor(0.25f, 1f, 1f)
            ranges[2] -> Color.getHSBColor(0.2f, 1f, 1f)
            ranges[3] -> Color.getHSBColor(0.15f, 1f, 1f)
            ranges[4] -> Color.getHSBColor(0.1f, 1f, 1f)
            ranges[5] -> Color.getHSBColor(0.05f, 1f, 1f)
            else -> Color.getHSBColor(0f, 1f, 1f)
        }
    }

    override fun applyAdditionalFilters(filteredGames: List<GameWrapper>) =
        filteredGames.filter { RoundTheClockConfig.fromJson(it.gameParams).inOrder }

    override fun getTableRows(filteredGames: List<GameWrapper>): Pair<List<List<Any?>>, List<Any>>
    {
        val breakdownRows = mutableListOf<List<Any?>>()

        val hmTargetToAverageDarts = getAverageThrowsPerTarget(filteredGames)
        val hmTargetToRangeBreakdown = getRangeBreakdownPerTarget(filteredGames)

        val hmRangeToOverallCount = HashMapCount<IntRange>()
        hmTargetToRangeBreakdown.forEach{ target, rangeToCount ->
            val avg = hmTargetToAverageDarts[target]

            val row = listOf(target) + ranges.map { rangeToCount.getCount(it) } + listOf(avg)
            breakdownRows.add(row)

            ranges.forEach{ hmRangeToOverallCount.incrementCount(it, rangeToCount.getCount(it)) }
        }

        //Overall
        val overallAverage = hmTargetToAverageDarts.values.sum() / 20
        val totalRow = listOf("Overall") + ranges.map{ hmRangeToOverallCount.getCount(it) } + listOf(overallAverage)
        return Pair(breakdownRows, totalRow)
    }

    private fun getAverageThrowsPerTarget(games: List<GameWrapper>): Map<Int, Double>
    {
        return games.flatMap{ it.getAllDarts() }
                    .groupBy{ it.startingScore }
                    .mapValues{ it.value.size.toDouble() / games.size }
    }

    private fun getRangeBreakdownPerTarget(games: List<GameWrapper>): Map<Int, HashMapCount<IntRange>>
    {
        val hmRangeBreakdown = mutableMapOf<Int, HashMapCount<IntRange>>()

        val hmCountConstructor = { HashMapCount<IntRange>() }
        val individualGameRanges = games.map{ it.getRangeByTarget(ranges) }
        individualGameRanges.forEach{
            it.forEach{ target, range ->
                val hmCount = hmRangeBreakdown.getOrPut(target, hmCountConstructor)
                hmCount.incrementCount(range)
            }
        }

        return hmRangeBreakdown
    }
}