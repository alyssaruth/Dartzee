package burlton.dartzee.test.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOdd
import burlton.dartzee.code.dartzee.generateAllPossibilities
import burlton.dartzee.code.dartzee.getValidSegments
import burlton.dartzee.test.borrowTestDartboard
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test

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