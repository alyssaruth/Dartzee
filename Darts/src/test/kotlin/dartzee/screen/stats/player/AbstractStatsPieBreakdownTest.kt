package dartzee.test.screen.stats.player

import dartzee.screen.stats.player.AbstractStatisticsTabPieBreakdown
import io.kotlintest.matchers.collections.shouldContainNoNulls
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Component

abstract class AbstractStatsPieBreakdownTest<E: AbstractStatisticsTabPieBreakdown>: AbstractPlayerStatisticsTest<E>()
{
    abstract fun getAllPossibilitiesForScores(): IntRange

    override fun getComponentsForComparison(tab: E): List<Component>
    {
        return listOf(tab.otherPieChartPanel, tab.tableHoleBreakdownOther)
    }

    @Test
    fun `Ranges should be distinct and cover all possibilities`()
    {
        val ranges = factoryTab().ranges
        for (i in getAllPossibilitiesForScores())
        {
            ranges.filter{ it.contains(i) }.shouldHaveSize(1)
        }
    }

    @Test
    fun `Should return a different, valid colour for all ranges`()
    {
        val tab = factoryTab()

        val colours = tab.ranges.map{ tab.getColorForRange(it) }.distinct()

        colours.size shouldBe tab.ranges.size
        colours.shouldContainNoNulls()
    }
}