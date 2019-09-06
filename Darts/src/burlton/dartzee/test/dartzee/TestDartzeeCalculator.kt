package burlton.dartzee.test.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE
import burlton.dartzee.code.dartzee.dart.*
import burlton.dartzee.code.dartzee.generateAllPossibilities
import burlton.dartzee.code.dartzee.generateRuleDescription
import burlton.dartzee.code.dartzee.getValidSegments
import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleGreaterThan
import burlton.dartzee.code.dartzee.total.DartzeeTotalRulePrime
import burlton.dartzee.test.borrowTestDartboard
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test

class TestDartzeeRuleDescriptions: AbstractDartsTest()
{
    @Test
    fun `Should describe total rules correctly`()
    {
        val rule = makeDartzeeRuleDto(totalRule = DartzeeTotalRulePrime())
        rule.generateRuleDescription() shouldBe "Total is prime"

        val rule2 = makeDartzeeRuleDto(totalRule = DartzeeTotalRuleGreaterThan())
        rule2.generateRuleDescription() shouldBe "Total > 20"
    }

    @Test
    fun `Should describe in-order dart rules`()
    {
        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven(), DartzeeDartRuleOdd(), DartzeeDartRuleEven(), inOrder = true)
        rule.generateRuleDescription() shouldBe "Even → Odd → Even"
    }

    @Test
    fun `Should condense the same rules if order isn't required`()
    {
        val rule = makeDartzeeRuleDto(DartzeeDartRuleInner(), DartzeeDartRuleOuter(), DartzeeDartRuleOuter(), inOrder = false)
        rule.generateRuleDescription() shouldBe "{ 2x Outer, 1x Inner }"
    }

    @Test
    fun `Should ignore Any rules if order isn't required`()
    {
        val rule = makeDartzeeRuleDto(DartzeeDartRuleInner(), DartzeeDartRuleOuter(), DartzeeDartRuleAny(), inOrder = false)
        rule.generateRuleDescription() shouldBe "{ 1x Inner, 1x Outer }"
    }

    @Test
    fun `Should return Anything for a totally empty rule`()
    {
        val rule = makeDartzeeRuleDto()
        rule.generateRuleDescription() shouldBe "Anything"
    }

    @Test
    fun `Should return Anything for a rule with Any dart rules`()
    {
        val unorderedRule = makeDartzeeRuleDto(DartzeeDartRuleAny(), DartzeeDartRuleAny(), DartzeeDartRuleAny(), inOrder = false)
        val orderedRule = makeDartzeeRuleDto(DartzeeDartRuleAny(), DartzeeDartRuleAny(), DartzeeDartRuleAny(), inOrder = false)

        unorderedRule.generateRuleDescription() shouldBe "Anything"
        orderedRule.generateRuleDescription() shouldBe "Anything"
    }

    @Test
    fun `Should describe 'score' dart rules`()
    {
        val scoreRule = DartzeeDartRuleScore()
        scoreRule.score = 15

        val rule = makeDartzeeRuleDto(scoreRule)
        rule.generateRuleDescription() shouldBe "Score 15"
    }

    @Test
    fun `Dart and total rules should be concatenated if both are present`()
    {
        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven(), totalRule = DartzeeTotalRuleGreaterThan())
        rule.generateRuleDescription() shouldBe "Score Even, Total > 20"
    }
}

class TestAllPossibilities: AbstractDartsTest()
{
    @Test
    fun `Should generate the right number of possibilities if given no darts`()
    {
        val dartboard = borrowTestDartboard()

        val possibilities = generateAllPossibilities(dartboard, listOf(), false)
        possibilities.size shouldBe 82 * 82 * 82
        possibilities.distinct().size shouldBe 82 * 82 * 82
        possibilities.all { it.size == 3 } shouldBe true

        val possibilitiesWithMisses = generateAllPossibilities(dartboard, listOf(), true)
        possibilitiesWithMisses.size shouldBe 83 * 83 * 83
        possibilities.all { it.size == 3 } shouldBe true
    }

    @Test
    fun `Should generate the right number of possibilities if given 1 starting dart`()
    {
        val dartboard = borrowTestDartboard()

        val dart = Dart(20, 3)
        dart.segmentType = SEGMENT_TYPE_TREBLE

        var possibilities = generateAllPossibilities(dartboard, listOf(dart), false)
        possibilities.size shouldBe 82 * 82
        possibilities.all { it.size == 3} shouldBe true
        possibilities.all { it.first().scoreAndType == "20_$SEGMENT_TYPE_TREBLE" } shouldBe true

        possibilities = generateAllPossibilities(dartboard, listOf(dart), true)
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

        var possibilities = generateAllPossibilities(dartboard, listOf(dartOne, dartTwo), false)
        possibilities.size shouldBe 82
        possibilities.all { it.size == 3 } shouldBe true
        possibilities.all { it.first().scoreAndType == "20_$SEGMENT_TYPE_TREBLE" } shouldBe true
        possibilities.all { it[1].scoreAndType == "19_$SEGMENT_TYPE_DOUBLE" } shouldBe true

        possibilities = generateAllPossibilities(dartboard, listOf(dartOne, dartTwo), true)
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
        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven(), DartzeeDartRuleOdd(), DartzeeDartRuleEven(), inOrder = true)

        val dartboard = borrowTestDartboard()

        val segments = rule.getValidSegments(dartboard, listOf()).validSegments

        segments.find { it.score == 20 } shouldNotBe null
        segments.find { it.score == 19 } shouldBe null
    }
}