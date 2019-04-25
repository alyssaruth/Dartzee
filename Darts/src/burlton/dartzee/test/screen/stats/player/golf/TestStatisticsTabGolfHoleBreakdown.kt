package burlton.dartzee.test.screen.stats.player.golf

import burlton.dartzee.code.screen.stats.player.golf.StatisticsTabGolfHoleBreakdown
import burlton.dartzee.test.screen.stats.player.AbstractStatsPieBreakdownTest

class TestStatisticsTabGolfHoleBreakdown: AbstractStatsPieBreakdownTest<StatisticsTabGolfHoleBreakdown>()
{
    override fun factoryTab() = StatisticsTabGolfHoleBreakdown()
    override fun getAllPossibilitiesForScores() = 1..5
}