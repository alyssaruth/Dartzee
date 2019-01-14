package burlton.dartzee.test

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.utils.*
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class TestX01Util
{
    @Test
    fun testIsBust()
    {
        assertBust(-5, Dart(2, 2), true)
        assertBust(-5, Dart(2, 0), true)
        assertBust(-8, Dart(4, 2), true)

        assertBust(0, Dart(10, 1), true)
        assertBust(0, Dart(20, 3), true)

        assertBust(1, Dart(20, 2), true)

        assertBust(0, Dart(20, 2), false)
        assertBust(0, Dart(25, 2), false)

        assertBust(20, Dart(20, 2), false)
        assertBust(20, Dart(20, 1), false)
    }

    private fun assertBust(score: Int, drt: Dart, expected: Boolean)
    {
        Assert.assertTrue("Bust in X01 if ended on $score after $drt", isBust(score, drt) == expected)
    }

    @Test
    fun testShouldStopForMercyRule()
    {
        val model = AbstractDartsModel.factoryForType(AbstractDartsModel.TYPE_NORMAL_DISTRIBUTION)
        model!!.mercyThreshold = 19

        assertShouldStopForMercyRule(model, 19, 16, false)
        assertShouldStopForMercyRule(model, 17, 16, true)
        assertShouldStopForMercyRule(model, 15, 8, true)
        assertShouldStopForMercyRule(model, 16, 8, false)
        assertShouldStopForMercyRule(model, 17, 13, false)
        assertShouldStopForMercyRule(model, 17, 17, false)

        model.mercyThreshold = -1

        assertShouldStopForMercyRule(model, 19, 16, false)
        assertShouldStopForMercyRule(model, 17, 16, false)
        assertShouldStopForMercyRule(model, 15, 8, false)
        assertShouldStopForMercyRule(model, 16, 8, false)
        assertShouldStopForMercyRule(model, 17, 13, false)
        assertShouldStopForMercyRule(model, 17, 17, false)
    }

    private fun assertShouldStopForMercyRule(model: AbstractDartsModel, startingScore: Int, currentScore: Int, expected: Boolean)
    {
        val result = shouldStopForMercyRule(model, startingScore, currentScore)

        val desc = "Mercy Rule - Threshold [" + model.mercyThreshold + "], Start [" + startingScore + "], current [" + currentScore + "]"
        Assert.assertTrue(desc, result == expected)
    }

    @Test
    fun testIsCheckoutDart()
    {
        assertCheckout(52, false)
        assertCheckout(50, true)
        assertCheckout(45, false)
        assertCheckout(42, false)
        assertCheckout(41, false)
        assertCheckout(40, true)
        assertCheckout(35, false)
        assertCheckout(2, true)
    }

    private fun assertCheckout(startingScore: Int, expected: Boolean)
    {
        val drt = Dart(20, 2)
        drt.startingScore = startingScore

        val result = isCheckoutDart(drt)

        Assert.assertTrue("Is a checkout dart for starting score $startingScore", result == expected)
    }

    @Test
    fun testIsFinishRound()
    {
        val d = Dart(20, 1)
        d.startingScore = 20

        val round = mutableListOf(Dart(2, 1), d)

        assertIsFinishRound(round, false)
        d.multiplier = 2
        assertIsFinishRound(round, false)
        d.score = 10
        assertIsFinishRound(round, true)
    }
    private fun assertIsFinishRound(round: MutableList<Dart>, expected: Boolean)
    {
        val actual = isFinishRound(round)

        assertEquals(expected, actual, "Is finish round $round")
    }

    @Test
    fun testGetScoringDartsNull()
    {
        val result = getScoringDarts(null, 20)

        assertEquals(mutableListOf(), result)
    }


}