package dartzee.screen.stats.player

import io.kotest.matchers.collections.shouldContainNoNulls
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Component

abstract class AbstractStatsPieBreakdownTest<E: AbstractStatisticsTabPieBreakdown>: AbstractPlayerStatisticsTest<E>()
{
    abstract fun getAllPossibilitiesForScores(): IntRange

    override fun getComponentsForComparison(tab: E): List<Component> =
        listOf(tab.otherPieChartPanel, tab.tableHoleBreakdownOther)

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