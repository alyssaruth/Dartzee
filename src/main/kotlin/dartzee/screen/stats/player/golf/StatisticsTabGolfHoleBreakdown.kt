package dartzee.screen.stats.player.golf

import dartzee.screen.game.scorer.getGolfScorerColour
import dartzee.screen.stats.player.AbstractStatisticsTabPieBreakdown
import dartzee.screen.stats.player.HoleBreakdownWrapper
import dartzee.stats.GameWrapper
import java.awt.Color

class StatisticsTabGolfHoleBreakdown : AbstractStatisticsTabPieBreakdown() {
    override val ranges = listOf(1..1, 2..2, 3..3, 4..4, 5..5)

    override fun getColorForRange(range: IntRange): Color = getGolfScorerColour(range.first, 1.0)

    override fun getTableRows(
        filteredGames: List<GameWrapper>
    ): Pair<List<List<Any?>>, List<Any>?> {
        val breakdownRows = mutableListOf<List<Any?>>()

        val hm = mutableMapOf<Int, HoleBreakdownWrapper>()

        filteredGames.forEach { it.updateHoleBreakdowns(hm) }

        val overall = hm.remove(-1)

        hm.forEach { (hole, bd) ->
            val row = bd.getAsTableRow(hole)
            breakdownRows.add(row)
        }

        return Pair(breakdownRows, overall?.getAsTableRow("Overall"))
    }
}
