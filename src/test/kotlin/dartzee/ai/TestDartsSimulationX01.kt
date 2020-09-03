package dartzee.ai

import dartzee.`object`.Dart
import dartzee.makeTestDartboard
import dartzee.helper.AbstractTest
import dartzee.helper.beastDartsModel
import dartzee.helper.insertPlayer
import dartzee.helper.predictableX01Model
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartsSimulationX01: AbstractTest()
{
    @Test
    fun `Should simulate a game correctly for a perfect player`()
    {
        val model = beastDartsModel(hmScoreToDart = mapOf(81 to AimDart(19, 3)))
        val player = insertPlayer(model = model)

        val dartboard = makeTestDartboard()

        val simulation = DartsSimulationX01(dartboard, player, model)
        val result = simulation.simulateGame(-1)

        result.finalScore shouldBe 9
        val darts = result.getAllDarts()
        darts.shouldContainExactly(
            Dart(20, 3), Dart(20, 3), Dart(20, 3),
            Dart(20, 3), Dart(20, 3), Dart(20, 3),
            Dart(20, 3), Dart(19, 3), Dart(12, 2))

        result.getCheckoutTotal() shouldBe 141
    }

    @Test
    fun `Should take into account mercy rule`()
    {
        val player = insertPlayer()
        val dartboard = makeTestDartboard()

        // T20, T20, T20
        // T20, T20, T20
        // T20, T20,  18 (on 3)
        //   1 (mercy)
        //  D1
        val model = predictableX01Model(dartboard, mercyThreshold = 7) { startingScore, _ ->
            when (startingScore)
            {
                21 -> AimDart(18, 1)
                3 -> AimDart(1, 1)
                2 -> AimDart(1, 2)
                else -> AimDart(20, 3)
            }
        }

        val simulation = DartsSimulationX01(dartboard, player, model)
        val result = simulation.simulateGame(-1)
        result.finalScore shouldBe 13
    }

    @Test
    fun `Should take into account busts`()
    {
        val player = insertPlayer()
        val dartboard = makeTestDartboard()

        // T20, T20, T20
        // T20, T20, T20
        // T20, T20,   1 (on 20)
        // D15 (bust)
        // D10
        val model = predictableX01Model(dartboard, mercyThreshold = 7) { startingScore, dartsThrown ->
            when (startingScore)
            {
                21 -> AimDart(1, 1)
                20 -> if (dartsThrown == 9) AimDart(15, 2) else AimDart(10, 2)
                else -> AimDart(20, 3)
            }
        }

        val simulation = DartsSimulationX01(dartboard, player, model)
        val result = simulation.simulateGame(-1)
        result.finalScore shouldBe 13
    }
}