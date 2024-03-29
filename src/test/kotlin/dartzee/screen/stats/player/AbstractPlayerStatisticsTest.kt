package dartzee.screen.stats.player

import dartzee.core.bean.ScrollTable
import dartzee.core.util.containsComponent
import dartzee.helper.AbstractTest
import dartzee.helper.makeGameWrapper
import dartzee.stats.GameWrapper
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import java.awt.Color
import java.awt.Component
import org.junit.jupiter.api.Test

abstract class AbstractPlayerStatisticsTest<E : AbstractStatisticsTab> : AbstractTest() {
    abstract fun factoryTab(): E

    abstract fun getComponentsForComparison(tab: E): List<Component>

    open fun factoryGameWrapper(): GameWrapper = makeGameWrapper()

    @Test
    fun `Components for comparison should have red foregrounds`() {
        val components = getComponentsForComparison(factoryTab())
        components.forEach {
            when (it) {
                is ScrollTable -> it.tableForeground shouldBe Color.RED
            }
        }
    }

    @Test
    fun `Should show or hide comparison components`() {
        val tab = factoryTab()
        val components = getComponentsForComparison(tab)

        tab.setFilteredGames(listOf(factoryGameWrapper()), listOf())
        tab.populateStats()
        components.forEach { tab.containsComponent(it) shouldBe false }

        tab.setFilteredGames(listOf(factoryGameWrapper()), listOf(factoryGameWrapper()))
        tab.populateStats()
        components.forEach { tab.containsComponent(it) shouldBe true }

        tab.setFilteredGames(listOf(factoryGameWrapper()), listOf())
        tab.populateStats()
        components.forEach { tab.containsComponent(it) shouldBe false }
    }

    @Test
    fun `It should handle displaying no games`() {
        shouldNotThrowAny {
            val tab = factoryTab()
            tab.setFilteredGames(listOf(), listOf())

            tab.populateStats()
        }
    }
}
