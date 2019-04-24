package burlton.dartzee.test.screen.stats.player.rtc

import burlton.dartzee.code.screen.stats.player.rtc.StatisticsTabRoundTheClockHitRate
import burlton.dartzee.test.screen.stats.player.AbstractPlayerStatisticsTest
import java.awt.Component

class TestStatisticsTabRoundTheClockHitRate: AbstractPlayerStatisticsTest<StatisticsTabRoundTheClockHitRate>()
{
    override fun factoryTab() = StatisticsTabRoundTheClockHitRate()
    override fun getComponentsForComparison(tab: StatisticsTabRoundTheClockHitRate): List<Component>
    {
        return listOf(tab.otherPieChartPanel, tab.tableHoleBreakdownOther)
    }
}