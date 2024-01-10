package dartzee.ai

import dartzee.bean.PresentationDartboard
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.`object`.SegmentType
import dartzee.screen.stats.median
import io.kotest.matchers.doubles.shouldBeBetween
import java.awt.Dimension
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

private const val SIMPLE_SIMULATION_TOLERANCE = 1.0

class DartsAiModelBenchmarkTest : AbstractTest() {
    private val alanPartridge =
        DartsAiModel(
            170.0,
            160.0,
            80.0,
            129,
            20,
            emptyMap(),
            18,
            mapOf(1 to SegmentType.DOUBLE, 2 to SegmentType.DOUBLE, 3 to SegmentType.DOUBLE),
            mapOf(1 to 3, 2 to 4),
            DartzeePlayStyle.CAUTIOUS
        )

    private val bruceForsyth =
        DartsAiModel(
            90.0,
            180.0,
            90.0,
            101,
            20,
            emptyMap(),
            null,
            mapOf(
                1 to SegmentType.INNER_SINGLE,
                2 to SegmentType.INNER_SINGLE,
                3 to SegmentType.OUTER_SINGLE
            ),
            mapOf(1 to 3, 2 to 4),
            DartzeePlayStyle.CAUTIOUS
        )

    @Test
    @Tag("integration")
    fun `Alan Partridge - Simple simulation`() {
        val dartboard = PresentationDartboard().also { it.size = Dimension(400, 400) }

        repeat(5) {
            val result = DartsAiSimulator.runSimulation(alanPartridge, dartboard)
            result.averageDart.shouldBeBetween(14.5, 15.5, SIMPLE_SIMULATION_TOLERANCE)
            result.missPercent.shouldBeBetween(4.5, 5.5, SIMPLE_SIMULATION_TOLERANCE)
            result.treblePercent.shouldBeBetween(6.6, 7.6, SIMPLE_SIMULATION_TOLERANCE)
            result.finishPercent.shouldBeBetween(7.5, 8.5, SIMPLE_SIMULATION_TOLERANCE)
        }
    }

    @Test
    @Tag("integration")
    fun `Alan Partridge - Full games`() {
        val simulation = DartsSimulationX01(insertPlayer(), alanPartridge)

        val results = (1..5000).map { simulation.simulateGame(-it.toLong()).finalScore }
        results.average().shouldBeBetween(50.5, 51.5, SIMPLE_SIMULATION_TOLERANCE)
        results.median().shouldBeBetween(46.0, 47.0, SIMPLE_SIMULATION_TOLERANCE)
    }

    @Test
    @Tag("integration")
    fun `Bruce Forsyth - Simple simulation`() {
        val dartboard = PresentationDartboard().also { it.size = Dimension(400, 400) }

        repeat(5) {
            val result = DartsAiSimulator.runSimulation(bruceForsyth, dartboard)
            result.averageDart.shouldBeBetween(16.5, 17.0, SIMPLE_SIMULATION_TOLERANCE)
            result.missPercent.shouldBeBetween(2.3, 2.6, 0.5)
            result.treblePercent.shouldBeBetween(10.1, 10.8, SIMPLE_SIMULATION_TOLERANCE)
            result.finishPercent.shouldBeBetween(9.8, 10.3, SIMPLE_SIMULATION_TOLERANCE)
        }
    }

    @Test
    @Tag("integration")
    fun `Bruce Forsyth - Full games`() {
        val simulation = DartsSimulationX01(insertPlayer(), bruceForsyth)

        val results = (1..5000).map { simulation.simulateGame(-it.toLong()).finalScore }
        results.average().shouldBeBetween(44.5, 45.0, SIMPLE_SIMULATION_TOLERANCE)
        results.median().shouldBeBetween(40.0, 40.0, SIMPLE_SIMULATION_TOLERANCE)
    }
}
