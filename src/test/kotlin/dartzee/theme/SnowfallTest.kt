package dartzee.theme

import io.github.alyssaruth.swingtest.shouldMatchImage
import java.awt.Dimension
import kotlin.random.Random
import org.junit.Test
import org.junit.jupiter.api.Tag

class SnowfallTest {
    @Test
    @Tag("screenshot")
    fun `should smooth a spiky snow heap`() {
        val heights =
            listOf(0, 5, 2, 0, 0, 7, 3, 1, 0, 1, 1, 0, 1, 2, 3, 4)
                .mapIndexed { index, height -> index to height }
                .toMap()
                .toMutableMap()

        val snowfall = Snowfall(heights, maxFlakeChance = 0)
        snowfall.size = Dimension(heights.size, 10)
        snowfall.active = true

        snowfall.shouldMatchImage("heap-0")
        snowfall.doTick()
        snowfall.shouldMatchImage("heap-1")
        snowfall.doTick()
        snowfall.shouldMatchImage("heap-2")
        snowfall.doTick()
        snowfall.shouldMatchImage("heap-3")

        repeat(3) {
            snowfall.doTick()
            snowfall.shouldMatchImage("heap-3")
        }
    }

    @Test
    @Tag("screenshot")
    fun `Should match screenshot`() {
        val snowfall = Snowfall(random = Random(12345))
        snowfall.size = Dimension(100, 100)
        snowfall.active = true

        repeat(50) { snowfall.doTick() }
        snowfall.shouldMatchImage("snowfall-50")

        snowfall.doTick()
        snowfall.shouldMatchImage("snowfall-51")

        repeat(49) { snowfall.doTick() }
        snowfall.shouldMatchImage("snowfall-100")

        repeat(100) { snowfall.doTick() }
        snowfall.shouldMatchImage("snowfall-200")

        repeat(100) { snowfall.doTick() }
        snowfall.shouldMatchImage("snowfall-300")

        repeat(200) { snowfall.doTick() }
        snowfall.shouldMatchImage("snowfall-500")
    }
}
