package burlton.dartzee.test

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.utils.*
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.mock
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun testGetScoringDarts()
    {
        val d1 = mock(Dart::class.java)
        val d2 = mock(Dart::class.java)
        val d3 = mock(Dart::class.java)
        d1.startingScore = 51
        d2.startingScore = 50
        d3.startingScore = 49

        val list = mutableListOf(d1, d2, d3)

        val result = getScoringDarts(list, 50)

        assertTrue(result.contains(d1))
        assertFalse(result.contains(d2))
        assertFalse(result.contains(d2))
    }

    @Test
    fun testCalculateThreeDartAverage()
    {
        val d1 = Dart(20, 1)
        val d2 = Dart(20, 2)
        val d3 = Dart(10, 0)
        val d4 = Dart(5, 3)

        d1.startingScore = 100
        d2.startingScore = 100
        d3.startingScore = 80
        d4.startingScore = 100

        val list = mutableListOf(d1, d2, d3, d4)
        val result = calculateThreeDartAverage(list, 70)
        val resultTwo = calculateThreeDartAverage(list, 90) //The miss should be excluded
        val resultThree = calculateThreeDartAverage(list, 200) //Test an empty list

        assertThat(result, equalTo(56.25))
        assertThat(resultTwo, equalTo(75.0))
        assertThat(resultThree, equalTo(-1.0))
    }

    @Test
    fun testSumScore()
    {
        val d1 = Dart(20, 2)
        val d2 = Dart(13, 0)
        val d3 = Dart(11, 1)

        val list = mutableListOf(d1, d2, d3)


        val sum = sumScore(list)
        assertThat(sum, equalTo(51))
    }

    @Test
    fun testIsShanghai()
    {
        val tooShort = mutableListOf(Dart(20, 3), Dart(20, 3))
        val wrongSum = mutableListOf(Dart(20, 1), Dart(20, 3), Dart(20, 3))
        val allDoubles = mutableListOf(Dart(20, 2), Dart(20, 2), Dart(20, 2))
        val correct = mutableListOf(Dart(20, 1), Dart(20, 2), Dart(20, 3))
        val correctDifferentOrder = mutableListOf(Dart(20, 2), Dart(20, 3), Dart(20, 1))

        assertFalse(isShanghai(tooShort))
        assertFalse(isShanghai(wrongSum))
        assertFalse(isShanghai(allDoubles))

        assertTrue(isShanghai(correct))
        assertTrue(isShanghai(correctDifferentOrder))
    }

    @Test
    fun testGetSortedDartStr()
    {
        val listOne = mutableListOf(Dart(2, 3), Dart(3, 2), Dart(20, 1))
        val listTwo = mutableListOf(Dart(1, 1), Dart(7, 1), Dart(5, 1))
        val listThree = mutableListOf(Dart(20, 3), Dart(20, 3), Dart(20, 3))
        val listFour = mutableListOf(Dart(25, 2), Dart(20, 3), Dart(20, 0))

        assertThat(getSortedDartStr(listOne), equalTo("20, T2, D3"))
        assertThat(getSortedDartStr(listTwo), equalTo("7, 5, 1"))
        assertThat(getSortedDartStr(listThree), equalTo("T20, T20, T20"))
        assertThat(getSortedDartStr(listFour), equalTo("T20, D25, 0"))
    }
}