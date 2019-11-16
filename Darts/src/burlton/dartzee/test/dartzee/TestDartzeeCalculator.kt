package burlton.dartzee.test.dartzee

import burlton.core.code.util.getAllPermutations
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE
import burlton.dartzee.code.dartzee.DartzeeCalculator
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOdd
import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleEqualTo
import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleEven
import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleLessThan
import burlton.dartzee.test.*
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.makeDartzeeRuleDto
import burlton.dartzee.test.helper.makeScoreRule
import burlton.dartzee.test.helper.makeTotalScoreRule
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test

class TestAllPossibilities: AbstractDartsTest()
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

class TestValidSegments: AbstractDartsTest()
{
    @Test
    fun `getValidSegments should just filter by the ones that are valid`()
    {
        val rule = makeDartzeeRuleDto(
            DartzeeDartRuleEven(),
            DartzeeDartRuleOdd(),
            DartzeeDartRuleEven(),
            inOrder = true
        )

        val segments = DartzeeCalculator().getValidSegments(rule, listOf()).validSegments

        segments.find { it.score == 20 } shouldNotBe null
        segments.find { it.score == 19 } shouldBe null
    }

    @Test
    fun `should combine total and darts rules correctly`()
    {
        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven(), DartzeeDartRuleEven(), DartzeeDartRuleEven(), makeTotalScoreRule<DartzeeTotalRuleLessThan>(20))

        val segments = DartzeeCalculator().getValidSegments(rule, listOf()).validSegments

        segments.find { it.score == 16 } shouldBe null
    }

    @Test
    fun `should not cache results between calculations`()
    {
        val ruleOne = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(19), makeScoreRule(18), inOrder = false)
        val ruleTwo = makeDartzeeRuleDto(makeScoreRule(1), makeScoreRule(2), makeScoreRule(3), inOrder = false)

        val calculator = DartzeeCalculator()
        val dartboard = borrowTestDartboard()

        val firstSegments = calculator.getValidSegments(ruleOne, listOf()).validSegments
        val secondSegments = calculator.getValidSegments(ruleTwo, listOf()).validSegments

        firstSegments.any { it.score == 20 } shouldBe true
        secondSegments.any { it.score == 20 } shouldBe false
    }
}

class TestValidCombinations: AbstractDartsTest()
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