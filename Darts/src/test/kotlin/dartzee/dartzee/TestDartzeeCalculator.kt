package dartzee.test.dartzee

import dartzee.core.util.getAllPermutations
import dartzee.`object`.*
import dartzee.dartzee.DartzeeCalculator
import dartzee.dartzee.dart.DartzeeDartRuleAny
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.dartzee.dart.DartzeeDartRuleOuter
import dartzee.dartzee.total.DartzeeTotalRuleEqualTo
import dartzee.dartzee.total.DartzeeTotalRuleEven
import dartzee.dartzee.total.DartzeeTotalRuleLessThan
import dartzee.utils.getAllPossibleSegments
import dartzee.test.*
import dartzee.test.helper.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.doubles.shouldBeBetween
import io.kotlintest.matchers.doubles.shouldBeExactly
import io.kotlintest.shouldBe
import org.junit.Test

class TestAllPossibilities: AbstractTest()
{
    @Test
    fun `Should generate the right number of possibilities if given no darts`()
    {
        val possibilities = DartzeeCalculator().generateAllPossibilities(listOf())
        possibilities.size shouldBe 83 * 83 * 83
        possibilities.all { it.size == 3 } shouldBe true
    }

    @Test
    fun `Should generate the right number of possibilities if given 1 starting dart`()
    {
        val dart = Dart(20, 3)
        dart.segmentType = SEGMENT_TYPE_TREBLE

        val possibilities = DartzeeCalculator().generateAllPossibilities(listOf(dart))
        possibilities.size shouldBe 83 * 83
        possibilities.all { it.size == 3} shouldBe true
        possibilities.all { it.first().scoreAndType == "20_$SEGMENT_TYPE_TREBLE" } shouldBe true
    }

    @Test
    fun `Should generate the right number of possibilities if given 2 starting darts`()
    {
        val dartOne = Dart(20, 3)
        dartOne.segmentType = SEGMENT_TYPE_TREBLE

        val dartTwo = Dart(19, 2)
        dartTwo.segmentType = SEGMENT_TYPE_DOUBLE

        val possibilities = DartzeeCalculator().generateAllPossibilities(listOf(dartOne, dartTwo))
        possibilities.size shouldBe 83
        possibilities.all { it.size == 3 } shouldBe true
        possibilities.all { it.first().scoreAndType == "20_$SEGMENT_TYPE_TREBLE" } shouldBe true
        possibilities.all { it[1].scoreAndType == "19_$SEGMENT_TYPE_DOUBLE" } shouldBe true
    }
}

class TestValidSegments: AbstractTest()
{
    @Test
    fun `getValidSegments should return the right results for the first dart`()
    {
        val rule = makeDartzeeRuleDto(
            DartzeeDartRuleEven(),
            DartzeeDartRuleOdd(),
            DartzeeDartRuleOuter(),
            inOrder = true
        )

        val firstSegments = DartzeeCalculator().getValidSegments(rule, listOf()).validSegments
        firstSegments.shouldContainExactlyInAnyOrder(getAllPossibleSegments().filter { it.score % 2 == 0 && !it.isMiss()})
    }

    @Test
    fun `should return the right results for the second dart`()
    {
        val rule = makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleOuter(),
                inOrder = true
        )

        val secondSegments = DartzeeCalculator().getValidSegments(rule, listOf(makeDart(2, 1, SEGMENT_TYPE_OUTER_SINGLE))).validSegments
        secondSegments.shouldContainExactlyInAnyOrder(getAllPossibleSegments().filter { it.score % 2 != 0 && !it.isMiss() })
    }

    @Test
    fun `should return the right results for the third dart`()
    {
        val rule = makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleOuter(),
                inOrder = true
        )

        val dartsThrown = listOf(makeDart(2, 1, SEGMENT_TYPE_OUTER_SINGLE), makeDart(3, 1, SEGMENT_TYPE_INNER_SINGLE))
        val thirdSegments = DartzeeCalculator().getValidSegments(rule, dartsThrown).validSegments
        thirdSegments.shouldContainExactlyInAnyOrder(getOuterSegments())
    }

    @Test
    fun `should return no results for darts 2 or 3 if the rule is already failed`()
    {
        val rule = makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleOuter(),
                inOrder = true
        )

        val secondSegments = DartzeeCalculator().getValidSegments(rule, listOf(makeDart(3, 1, SEGMENT_TYPE_OUTER_SINGLE))).validSegments
        secondSegments.shouldBeEmpty()

        val invalidSecondDart = listOf(makeDart(2, 1, SEGMENT_TYPE_OUTER_SINGLE), makeDart(2, 1, SEGMENT_TYPE_OUTER_SINGLE))
        val thirdSegments = DartzeeCalculator().getValidSegments(rule, invalidSecondDart).validSegments
        thirdSegments.shouldBeEmpty()
    }

    @Test
    fun `Should return empty list if three darts thrown and rule has failed`()
    {
        val rule = makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleOuter(),
                inOrder = true
        )

        val dartsThrown = listOf(makeDart(2, 1, SEGMENT_TYPE_DOUBLE), makeDart(19, 3, SEGMENT_TYPE_TREBLE), makeDart(20, 0, SEGMENT_TYPE_MISS))
        val result = DartzeeCalculator().getValidSegments(rule, dartsThrown)

        result.validSegments.shouldBeEmpty()
        result.percentage shouldBe 0.0
        result.validCombinationProbability shouldBe 0.0
    }

    @Test
    fun `Should return the segments for dart 3 if the rule has been passed`()
    {
        val rule = makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleOuter(),
                inOrder = true
        )

        val dartsThrown = listOf(makeDart(2, 1, SEGMENT_TYPE_DOUBLE), makeDart(19, 3, SEGMENT_TYPE_TREBLE), makeDart(20, 2, SEGMENT_TYPE_DOUBLE))
        val result = DartzeeCalculator().getValidSegments(rule, dartsThrown)

        result.validSegments.shouldContainExactlyInAnyOrder(getOuterSegments())
    }

    @Test
    fun `Should handle dart rules that can be in any order`()
    {
        val rule = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(19), makeScoreRule(18), inOrder = false)

        val validSegments = getAllPossibleSegments().filter { listOf(20, 19, 18).contains(it.score) && !it.isMiss() }
        val result = DartzeeCalculator().getValidSegments(rule, listOf())
        result.validSegments.shouldContainExactlyInAnyOrder(validSegments)

        val resultTwo = DartzeeCalculator().getValidSegments(rule, listOf(makeDart(20, 1, SEGMENT_TYPE_OUTER_SINGLE)))
        resultTwo.validSegments.shouldContainExactlyInAnyOrder(validSegments.filter { it.score != 20 })
    }

    @Test
    fun `Should yield sensible probabilities`()
    {
        val rule = makeDartzeeRuleDto(DartzeeDartRuleOdd(), DartzeeDartRuleAny(), DartzeeDartRuleAny(), inOrder = true, allowMisses = true)

        val result = DartzeeCalculator().getValidSegments(rule, listOf())
        result.percentage.shouldBeBetween(49.0, 51.0, 0.0)

        val resultTwo = DartzeeCalculator().getValidSegments(rule, listOf(makeDart(13, 1, SEGMENT_TYPE_OUTER_SINGLE)))
        resultTwo.percentage.shouldBeExactly(100.0)
    }

    @Test
    fun `should combine total and darts rules correctly`()
    {
        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven(), DartzeeDartRuleEven(), DartzeeDartRuleEven(), makeTotalScoreRule<DartzeeTotalRuleLessThan>(20), true)

        val segments = DartzeeCalculator().getValidSegments(rule, listOf()).validSegments

        segments.find { it.score == 16 } shouldBe null
    }

    @Test
    fun `should not cache results between calculations`()
    {
        val ruleOne = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(19), makeScoreRule(18), inOrder = false)
        val ruleTwo = makeDartzeeRuleDto(makeScoreRule(1), makeScoreRule(2), makeScoreRule(3), inOrder = false)

        val calculator = DartzeeCalculator()

        val firstSegments = calculator.getValidSegments(ruleOne, listOf()).validSegments
        val secondSegments = calculator.getValidSegments(ruleTwo, listOf()).validSegments

        firstSegments.any { it.score == 20 } shouldBe true
        secondSegments.any { it.score == 20 } shouldBe false
    }
}

class TestValidCombinations: AbstractTest()
{
    @Test
    fun `should correctly identify all permutations as valid if no ordering required`()
    {
        val rule = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(19), makeScoreRule(18), inOrder = false)

        val segments = listOf(doubleNineteen, singleTwenty, singleEighteen)

        val permutations = segments.getAllPermutations()

        permutations.forEach {
            DartzeeCalculator().isValidCombination(it, rule) shouldBe true
        }
    }

    @Test
    fun `should enforce ordering correctly`()
    {
        val rule = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(19), makeScoreRule(18), inOrder = true)

        val orderedSegments = listOf(singleTwenty, singleNineteen, singleEighteen)

        val permutations = orderedSegments.getAllPermutations()

        permutations.forEach {
            DartzeeCalculator().isValidCombination(it, rule) shouldBe (it == orderedSegments)
        }
    }

    @Test
    fun `should test for both total and dart rules`()
    {
        val rule = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(19), makeScoreRule(18), DartzeeTotalRuleEven(), true)

        DartzeeCalculator().isValidCombination(listOf(singleTwenty, singleNineteen, singleEighteen), rule) shouldBe false
        DartzeeCalculator().isValidCombination(listOf(singleEighteen, singleEighteen, singleEighteen), rule) shouldBe false
        DartzeeCalculator().isValidCombination(listOf(singleTwenty, doubleNineteen, singleEighteen), rule) shouldBe true
    }

    @Test
    fun `Should test for just a single dart rule`()
    {
        val rule = makeDartzeeRuleDto(makeScoreRule(20))

        DartzeeCalculator().isValidCombination(listOf(singleTwenty, singleTwenty, singleTwenty), rule) shouldBe true
        DartzeeCalculator().isValidCombination(listOf(singleTwenty, singleTwenty, singleNineteen), rule) shouldBe true
        DartzeeCalculator().isValidCombination(listOf(singleTwenty, singleNineteen, singleNineteen), rule) shouldBe true
        DartzeeCalculator().isValidCombination(listOf(singleNineteen, singleNineteen, singleNineteen), rule) shouldBe false
    }

    @Test
    fun `should test for just total rules`()
    {
        val rule = makeDartzeeRuleDto(totalRule = makeTotalScoreRule<DartzeeTotalRuleEqualTo>(50))

        DartzeeCalculator().isValidCombination(listOf(singleTwenty, singleTwenty, singleTen), rule) shouldBe true
        DartzeeCalculator().isValidCombination(listOf(outerBull, singleTwenty, singleFive), rule) shouldBe true
        DartzeeCalculator().isValidCombination(listOf(singleTwenty, singleTwenty, singleTwenty), rule) shouldBe false
    }

    @Test
    fun `should validate misses correctly`()
    {
        val rule = makeDartzeeRuleDto(allowMisses = false)
        val ruleWithMisses = makeDartzeeRuleDto(allowMisses = true)

        val combination = listOf(singleTwenty, missTwenty, singleTwenty)
        DartzeeCalculator().isValidCombination(combination, rule) shouldBe false
        DartzeeCalculator().isValidCombination(combination, ruleWithMisses) shouldBe true
    }
}