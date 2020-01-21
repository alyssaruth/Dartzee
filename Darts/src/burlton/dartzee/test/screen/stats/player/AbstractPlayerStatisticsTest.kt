package burlton.dartzee.test.screen.stats.player

import burlton.dartzee.code.screen.stats.player.AbstractStatisticsTab
import burlton.dartzee.code.stats.GameWrapper
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.code.core.bean.ScrollTable
import burlton.dartzee.code.core.util.DateStatics
import burlton.dartzee.code.core.util.containsComponent
import burlton.dartzee.code.core.util.getSqlDateNow
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import org.junit.Test
import java.awt.Color
import java.awt.Component
import java.sql.Timestamp

abstract class AbstractPlayerStatisticsTest<E: AbstractStatisticsTab>: AbstractDartsTest()
{
    abstract fun factoryTab(): E
    abstract fun getComponentsForComparison(tab: E): List<Component>

    @Test
    fun `Components for comparison should have red foregrounds`()
    {
        val components = getComponentsForComparison(factoryTab())
        components.forEach{
            when(it)
            {
                is ScrollTable -> it.tableForeground shouldBe Color.RED
            }
        }
    }

    @Test
    fun `Should show or hide comparison components`()
    {
        val tab = factoryTab()
        val components = getComponentsForComparison(tab)

        tab.setFilteredGames(listOf(constructGameWrapper()), listOf())
        tab.populateStats()
        components.forEach{
            containsComponent(tab, it) shouldBe false
        }

        tab.setFilteredGames(listOf(constructGameWrapper()), listOf(constructGameWrapper()))
        tab.populateStats()
        components.forEach{
            containsComponent(tab, it) shouldBe true
        }

        tab.setFilteredGames(listOf(constructGameWrapper()), listOf())
        tab.populateStats()
        components.forEach{
            containsComponent(tab, it) shouldBe false
        }
    }

    @Test
    fun `It should handle displaying no games`()
    {
        shouldNotThrowAny{
            val tab = factoryTab()
            tab.setFilteredGames(listOf(), listOf())

            tab.populateStats()
        }
    }

    fun constructGameWrapper(localId: Long = 1,
                             gameParams: String = "",
                             dtStart: Timestamp = getSqlDateNow(),
                             dtFinish: Timestamp = DateStatics.END_OF_TIME,
                             finalScore: Int = -1): GameWrapper
    {
        return GameWrapper(localId, gameParams, dtStart, dtFinish, finalScore)
    }
}