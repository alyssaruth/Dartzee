package burlton.dartzee.test.screen.stats.player.rtc

import burlton.dartzee.code.screen.stats.player.rtc.StatisticsTabRoundTheClockHitRate
import burlton.dartzee.test.screen.stats.player.AbstractPlayerStatisticsTest
import io.kotlintest.matchers.collections.shouldHaveSize
import org.junit.Test
import java.awt.Component

class TestStatisticsTabRoundTheClockHitRate: AbstractPlayerStatisticsTest<StatisticsTabRoundTheClockHitRate>()
{
    override fun factoryTab() = StatisticsTabRoundTheClockHitRate()
    override fun getComponentsForComparison(tab: StatisticsTabRoundTheClockHitRate): List<Component>
    {
        return listOf(tab.otherPieChartPanel, tab.tableHoleBreakdownOther)
    }

    @Test
    fun `Ranges should be distinct and cover all possibilities`()
    {
        val ranges = StatisticsTabRoundTheClockHitRate().ranges
        for (i in 1..1000)
        {
            ranges.filter{ it.contains(i) }.shouldHaveSize(1)
        }
    }
}