package burlton.dartzee.test.dartzee

import burlton.core.code.util.getAllPermutations
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE
import burlton.dartzee.code.dartzee.DartzeeCalculator
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOdd
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
        val dartboard = borrowTestDartboard()

        val possibilities = DartzeeCalculator().generateAllPossibilities(dartboard, listOf(), false)
        possibilities.size shouldBe 82 * 82 * 82
        possibilities.distinct().size shouldBe 82 * 82 * 82
        possibilities.all { it.size == 3 } shouldBe true

        val possibilitiesWithMisses = DartzeeCalculator().generateAllPossibilities(dartboard, listOf(), true)
        possibilitiesWithMisses.size shouldBe 83 * 83 * 83
        possibilities.all { it.size == 3 } shouldBe true
    }

    @Test
    fun `Should generate the right number of possibilities if given 1 starting dart`()
    {
        val dartboard = borrowTestDartboard()

        val dart = Dart(20, 3)
        dart.segmentType = SEGMENT_TYPE_TREBLE

        var possibilities = DartzeeCalculator().generateAllPossibilities(dartboard, listOf(dart), false)
        possibilities.size shouldBe 82 * 82
        possibilities.all { it.size == 3} shouldBe true
        possibilities.all { it.first().scoreAndType == "20_$SEGMENT_TYPE_TREBLE" } shouldBe true

        possibilities = DartzeeCalculator().generateAllPossibilities(dartboard, listOf(dart), true)
        possibilities.size shouldBe 83 * 83
        possibilities.all { it.size == 3} shouldBe true
        possibilities.all { it.first().scoreAndType == "20_$SEGMENT_TYPE_TREBLE" } shouldBe true
    }

    @Test
    fun `Should generate the right number of possibilities if given 2 starting darts`()
    {
        val dartboard = borrowTestDartboard()

        val dartOne = Dart(20, 3)
        dartOne.segmentType = SEGMENT_TYPE_TREBLE

        val dartTwo = Dart(19, 2)
        dartTwo.segmentType = SEGMENT_TYPE_DOUBLE

        var possibilities = DartzeeCalculator().generateAllPossibilities(dartboard, listOf(dartOne, dartTwo), false)
        possibilities.size shouldBe 82
        possibilities.all { it.size == 3 } shouldBe true
        possibilities.all { it.first().scoreAndType == "20_$SEGMENT_TYPE_TREBLE" } shouldBe true
        possibilities.all { it[1].scoreAndType == "19_$SEGMENT_TYPE_DOUBLE" } shouldBe true

        possibilities = DartzeeCalculator().generateAllPossibilities(dartboard, listOf(dartOne, dartTwo), true)
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

        val dartboard = borrowTestDartboard()

        val segments = DartzeeCalculator().getValidSegments(rule, dartboard, listOf()).validSegments

        segments.find { it.score == 20 } shouldNotBe null
        segments.find { it.score == 19 } shouldBe null
    }

    @Test
    fun `should combine total and darts rules correctly`()
    {
        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven(), DartzeeDartRuleEven(), DartzeeDartRuleEven(), makeTotalScoreRule<DartzeeTotalRuleLessThan>(20))

        val dartboard = borrowTestDartboard()

        val segments = DartzeeCalculator().getValidSegments(rule, dartboard, listOf()).validSegments

        segments.find { it.score == 16 } shouldBe null
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
}