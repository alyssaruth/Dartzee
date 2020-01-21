package dartzee.test.screen.stats.player.golf

import dartzee.screen.stats.player.golf.StatisticsTabGolfHoleBreakdown
import dartzee.test.screen.stats.player.AbstractStatsPieBreakdownTest

class TestStatisticsTabGolfHoleBreakdown: AbstractStatsPieBreakdownTest<StatisticsTabGolfHoleBreakdown>()
{
    override fun factoryTab() = StatisticsTabGolfHoleBreakdown()
    override fun getAllPossibilitiesForScores() = 1..5
}