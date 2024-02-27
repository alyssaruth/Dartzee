package dartzee.utils

import dartzee.game.FinishType
import dartzee.helper.AbstractTest
import dartzee.helper.makeDart
import dartzee.helper.makeDartsModel
import dartzee.helper.makeX01Rounds
import dartzee.`object`.Dart
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestX01Util : AbstractTest() {
    @Test
    fun `isBust should return the right values for finishType Doubles`() {
        isBust(makeDart(3, 1, startingScore = 4), FinishType.Doubles) shouldBe true
        isBust(makeDart(3, 1, startingScore = 3), FinishType.Doubles) shouldBe true
        isBust(makeDart(20, 3, startingScore = 60), FinishType.Doubles) shouldBe true

        isBust(makeDart(2, 2, startingScore = 5), FinishType.Doubles) shouldBe true
        isBust(makeDart(2, 2, startingScore = 4), FinishType.Doubles) shouldBe false
        isBust(makeDart(2, 2, startingScore = 3), FinishType.Doubles) shouldBe true

        isBust(makeDart(25, 2, startingScore = 50), FinishType.Doubles) shouldBe false
        isBust(makeDart(3, 1, startingScore = 5), FinishType.Doubles) shouldBe false
    }

    @Test
    fun `isBust should return correct values for finishType Any`() {
        isBust(makeDart(3, 1, startingScore = 4), FinishType.Any) shouldBe false
        isBust(makeDart(3, 1, startingScore = 3), FinishType.Any) shouldBe false
        isBust(makeDart(20, 3, startingScore = 60), FinishType.Any) shouldBe false

        isBust(makeDart(2, 2, startingScore = 5), FinishType.Any) shouldBe false
        isBust(makeDart(2, 2, startingScore = 4), FinishType.Any) shouldBe false
        isBust(makeDart(2, 2, startingScore = 3), FinishType.Any) shouldBe true

        isBust(makeDart(25, 2, startingScore = 50), FinishType.Any) shouldBe false
        isBust(makeDart(3, 1, startingScore = 5), FinishType.Any) shouldBe false
    }

    @Test
    fun `mercy rule`() {
        var model = makeDartsModel(mercyThreshold = 19)

        shouldStopForMercyRule(model, 19, 16, FinishType.Doubles).shouldBeFalse()
        shouldStopForMercyRule(model, 17, 16, FinishType.Doubles).shouldBeTrue()
        shouldStopForMercyRule(model, 15, 8, FinishType.Doubles).shouldBeTrue()
        shouldStopForMercyRule(model, 16, 8, FinishType.Doubles).shouldBeFalse()
        shouldStopForMercyRule(model, 17, 13, FinishType.Doubles).shouldBeFalse()
        shouldStopForMercyRule(model, 17, 17, FinishType.Doubles).shouldBeFalse()

        shouldStopForMercyRule(model, 15, 8, FinishType.Any).shouldBeFalse()

        model = makeDartsModel(mercyThreshold = null)

        shouldStopForMercyRule(model, 19, 16, FinishType.Doubles).shouldBeFalse()
        shouldStopForMercyRule(model, 17, 16, FinishType.Doubles).shouldBeFalse()
        shouldStopForMercyRule(model, 15, 8, FinishType.Doubles).shouldBeFalse()
        shouldStopForMercyRule(model, 16, 8, FinishType.Doubles).shouldBeFalse()
        shouldStopForMercyRule(model, 17, 13, FinishType.Doubles).shouldBeFalse()
        shouldStopForMercyRule(model, 17, 17, FinishType.Doubles).shouldBeFalse()
    }

    @Test
    fun testIsCheckoutDart() {
        assertCheckout(52, false)
        assertCheckout(50, true)
        assertCheckout(45, false)
        assertCheckout(42, false)
        assertCheckout(41, false)
        assertCheckout(40, true)
        assertCheckout(35, false)
        assertCheckout(2, true)
    }

    private fun assertCheckout(startingScore: Int, expected: Boolean) {
        val drt = Dart(20, 2)
        drt.startingScore = startingScore

        isCheckoutDart(drt) shouldBe expected
    }

    @Test
    fun testIsFinishRound() {
        isFinishRound(listOf(Dart(2, 1), makeDart(20, 1, startingScore = 20))).shouldBeFalse()
        isFinishRound(listOf(Dart(2, 1), makeDart(20, 2, startingScore = 20))).shouldBeFalse()
        isFinishRound(listOf(Dart(2, 1), makeDart(10, 2, startingScore = 20))).shouldBeTrue()
    }

    @Test
    fun testGetScoringDartsNull() {
        val result = getScoringDarts(null, 20)

        result.shouldBeEmpty()
    }

    @Test
    fun testGetScoringDarts() {
        val d1 = makeDart(1, 1, startingScore = 51)
        val d2 = makeDart(1, 1, startingScore = 50)
        val d3 = makeDart(20, 1, startingScore = 49)

        val list = mutableListOf(d1, d2, d3)

        val result = getScoringDarts(list, 50)
        result.shouldContainExactly(d1)
    }

    @Test
    fun `getScoringRounds should exclude rounds correctly`() {
        val round1 = listOf(Dart(20, 3), Dart(20, 1), Dart(5, 1)) // 115
        val round2 = listOf(Dart(20, 1), Dart(20, 1), Dart(1, 1)) //  74
        val round3 = listOf(Dart(9, 1), Dart(14, 1), Dart(1, 1)) //  50
        val rounds = makeX01Rounds(200, round1, round2, round3)

        getScoringRounds(rounds, 200).shouldBeEmpty()
        getScoringRounds(rounds, 75).shouldContainExactlyInAnyOrder(listOf(round1))
        getScoringRounds(rounds, 74).shouldContainExactlyInAnyOrder(round1, round2)
        getScoringRounds(rounds, 51).shouldContainExactlyInAnyOrder(round1, round2)
        getScoringRounds(rounds, 50).shouldContainExactlyInAnyOrder(round1, round2, round3)
    }

    @Test
    fun testCalculateThreeDartAverage() {
        val d1 = makeDart(20, 1, startingScore = 100)
        val d2 = makeDart(20, 2, startingScore = 100)
        val d3 = makeDart(10, 0, startingScore = 80)
        val d4 = makeDart(5, 3, startingScore = 100)

        val list = listOf(d1, d2, d3, d4)
        val result = calculateThreeDartAverage(list, 70)
        val resultTwo = calculateThreeDartAverage(list, 90) // The miss should be excluded
        val resultThree = calculateThreeDartAverage(list, 200) // Test an empty list

        result shouldBe 56.25
        resultTwo shouldBe 75.0
        resultThree shouldBe -1.0
    }

    @Test
    fun testSumScore() {
        val d1 = Dart(20, 2)
        val d2 = Dart(13, 0)
        val d3 = Dart(11, 1)

        val list = mutableListOf(d1, d2, d3)

        sumScore(list) shouldBe 51
    }

    @Test
    fun testIsShanghai() {
        val tooShort = mutableListOf(Dart(20, 3), Dart(20, 3))
        val miss = mutableListOf(Dart(20, 3), Dart(20, 3), Dart(20, 0))
        val wrongSum = mutableListOf(Dart(20, 1), Dart(20, 3), Dart(20, 3))
        val allDoubles = mutableListOf(Dart(20, 2), Dart(20, 2), Dart(20, 2))
        val correct = mutableListOf(Dart(20, 1), Dart(20, 2), Dart(20, 3))
        val correctDifferentOrder = mutableListOf(Dart(20, 2), Dart(20, 3), Dart(20, 1))

        isShanghai(tooShort).shouldBeFalse()
        isShanghai(miss).shouldBeFalse()
        isShanghai(wrongSum).shouldBeFalse()
        isShanghai(allDoubles).shouldBeFalse()

        isShanghai(correct).shouldBeTrue()
        isShanghai(correctDifferentOrder).shouldBeTrue()
    }

    @Test
    fun testGetSortedDartStr() {
        val listOne = mutableListOf(Dart(2, 3), Dart(3, 2), Dart(20, 1))
        val listTwo = mutableListOf(Dart(1, 1), Dart(7, 1), Dart(5, 1))
        val listThree = mutableListOf(Dart(20, 3), Dart(20, 3), Dart(20, 3))
        val listFour = mutableListOf(Dart(25, 2), Dart(20, 3), Dart(20, 0))

        getSortedDartStr(listOne) shouldBe "20, T2, D3"
        getSortedDartStr(listTwo) shouldBe "7, 5, 1"
        getSortedDartStr(listThree) shouldBe "T20, T20, T20"
        getSortedDartStr(listFour) shouldBe "T20, D25, 0"
    }

    @Test
    fun testIsNearMissDouble() {
        val nonCheckoutDart = Dart(16, 2)
        nonCheckoutDart.startingScore = 48

        val hitBullseye = Dart(25, 2)
        hitBullseye.startingScore = 50

        val missedBullseye = Dart(19, 1)
        missedBullseye.startingScore = 50

        val nearMissBullseye = Dart(25, 1)
        nearMissBullseye.startingScore = 50

        val nonAdjacentDoubleTop = Dart(12, 2)
        nonAdjacentDoubleTop.startingScore = 40

        val nonDoubleDoubleTop = Dart(5, 1)
        nonDoubleDoubleTop.startingScore = 40

        val nearMissDoubleTop = Dart(5, 2)
        nearMissDoubleTop.startingScore = 40

        isNearMissDouble(nonCheckoutDart).shouldBeFalse()
        isNearMissDouble(hitBullseye).shouldBeFalse()
        isNearMissDouble(missedBullseye).shouldBeFalse()
        isNearMissDouble(nearMissBullseye).shouldBeTrue()

        isNearMissDouble(nonAdjacentDoubleTop).shouldBeFalse()
        isNearMissDouble(nonDoubleDoubleTop).shouldBeFalse()
        isNearMissDouble(nearMissDoubleTop).shouldBeTrue()
    }
}
