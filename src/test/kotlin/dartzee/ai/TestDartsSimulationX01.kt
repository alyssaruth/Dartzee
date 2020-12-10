package dartzee.ai

import dartzee.`object`.Dart
import dartzee.helper.AbstractTest
import dartzee.helper.beastDartsModel
import dartzee.helper.insertPlayer
import dartzee.helper.predictableDartsModel
import dartzee.makeTestDartboard
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

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

        val aimDarts = listOf(
                AimDart(20, 3), AimDart(20, 3), AimDart(20, 3), //321
                AimDart(20, 3), AimDart(20, 3), AimDart(20, 3), //141
                AimDart(20, 3), AimDart(20, 3), AimDart(18, 1), //  3
                AimDart(1, 1),                                  //  2 (mercy)
                AimDart(1, 2)                                   //  0
        )

        val model = predictableDartsModel(dartboard, aimDarts, mercyThreshold = 7)

        val simulation = DartsSimulationX01(dartboard, player, model)
        val result = simulation.simulateGame(-1)
        result.finalScore shouldBe 13
    }

    @Test
    fun `Should take into account busts`()
    {
        val player = insertPlayer()
        val dartboard = makeTestDartboard()

        val aimDarts = listOf(
                AimDart(20, 3), AimDart(20, 3), AimDart(20, 3), //321
                AimDart(20, 3), AimDart(20, 3), AimDart(20, 3), //141
                AimDart(20, 3), AimDart(20, 3), AimDart(1, 1),  // 20
                AimDart(15, 2),                                 // 20 (bust)
                AimDart(10, 2)                                  //  0
        )

        val model = predictableDartsModel(dartboard, aimDarts, mercyThreshold = 7)
        val simulation = DartsSimulationX01(dartboard, player, model)
        val result = simulation.simulateGame(-1)
        result.finalScore shouldBe 13
    }
}