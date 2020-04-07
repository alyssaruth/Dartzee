package dartzee.dartzee

import dartzee.doubleNineteen
import dartzee.helper.AbstractTest
import dartzee.helper.makeDartzeeRuleCalculationResult
import dartzee.outerBull
import dartzee.singleTwenty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeRuleCalculationResult: AbstractTest()
{
    @Test
    fun `Should calculate the correct percentage based on the passed in probabilities`()
    {
        makeDartzeeRuleCalculationResult(validCombinationProbability = 20.0, allCombinationsProbability = 50.0).percentage shouldBe 40.0
        makeDartzeeRuleCalculationResult(validCombinationProbability = 0.0, allCombinationsProbability = 1.0).percentage shouldBe 0.0
        makeDartzeeRuleCalculationResult(validCombinationProbability = 0.9, allCombinationsProbability = 1.0).percentage shouldBe 90.0
        makeDartzeeRuleCalculationResult(validCombinationProbability = 0.985, allCombinationsProbability = 1.0).percentage shouldBe 98.5
        makeDartzeeRuleCalculationResult(validCombinationProbability = 27.0, allCombinationsProbability = 27.0).percentage shouldBe 100.0
    }

    @Test
    fun `Should show number of combinations, along with percentage`()
    {
        val result = makeDartzeeRuleCalculationResult(validCombinations = 256, validCombinationProbability = 20.0, allCombinationsProbability = 50.0)
        result.getCombinationsDesc() shouldBe "256 combinations (success%: 40.0%)"
    }

    @Test
    fun `Should report the correct difficulty`()
    {
        makeCalculationResult(0.0, 0).getDifficultyDesc() shouldBe "Impossible"
        makeCalculationResult(0.0, 1).getDifficultyDesc() shouldBe "Insane"
        makeCalculationResult(1.0).getDifficultyDesc() shouldBe "Insane"
        makeCalculationResult(1.1).getDifficultyDesc() shouldBe "Very Hard"
        makeCalculationResult(5.0).getDifficultyDesc() shouldBe "Very Hard"
        makeCalculationResult(5.1).getDifficultyDesc() shouldBe "Hard"
        makeCalculationResult(10.0).getDifficultyDesc() shouldBe "Hard"
        makeCalculationResult(10.1).getDifficultyDesc() shouldBe "Moderate"
        makeCalculationResult(25.0).getDifficultyDesc() shouldBe "Moderate"
        makeCalculationResult(25.1).getDifficultyDesc() shouldBe "Easy"
        makeCalculationResult(40.0).getDifficultyDesc() shouldBe "Easy"
        makeCalculationResult(40.1).getDifficultyDesc() shouldBe "Very Easy"
        makeCalculationResult(100.0).getDifficultyDesc() shouldBe "Very Easy"
    }

    @Test
    fun `Should externalise correctly`()
    {
        val result = DartzeeRuleCalculationResult(listOf(doubleNineteen),
            listOf(doubleNineteen, singleTwenty, outerBull),
            10,
            100,
            5.7,
            100.0)


        val dbStr = result.toDbString()

        val newResult = DartzeeRuleCalculationResult.fromDbString(dbStr)
        newResult.validCombinations shouldBe 10
        newResult.allCombinations shouldBe 100
        newResult.validCombinationProbability shouldBe 5.7
        newResult.allCombinationsProbability shouldBe 100.0
        newResult.scoringSegments.shouldContainExactly(doubleNineteen)
        newResult.validSegments.shouldContainExactlyInAnyOrder(doubleNineteen, singleTwenty, outerBull)
    }

    private fun makeCalculationResult(percentage: Double, validCombinations: Int = 100): DartzeeRuleCalculationResult
    {
        return makeDartzeeRuleCalculationResult(validCombinations = validCombinations, validCombinationProbability = percentage, allCombinationsProbability = 100.0)
    }
}