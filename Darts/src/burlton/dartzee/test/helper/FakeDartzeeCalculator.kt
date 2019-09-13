package burlton.dartzee.test.helper

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.`object`.SEGMENT_TYPE_INNER_SINGLE
import burlton.dartzee.code.dartzee.AbstractDartzeeCalculator
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.dartzee.DartzeeRuleCalculationResult
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.screen.dartzee.DartboardRuleVerifier

/**
 * Fast calculator for tests which assumes that the rule is always:
 *
 *  Inner single 1 -> Inner single 2 -> Inner single 3
 */
class FakeDartzeeCalculator: AbstractDartzeeCalculator()
{
    override fun getValidSegments(rule: DartzeeRuleDto, dartboard: Dartboard, dartsSoFar: List<Dart>): DartzeeRuleCalculationResult
    {
        val segments = mutableListOf<DartboardSegment>()
        if (isValidSoFar(dartsSoFar))
        {
            val nextSegment = dartboard.getFakeValidSegment(dartsSoFar.size)
            segments.add(nextSegment)
        }

        return DartzeeRuleCalculationResult(segments, segments.size, 10, 1.0, 1.0)
    }

    override fun isValidCombination(combination: List<DartboardSegment>, rule: DartzeeRuleDto): Boolean {
        return combination[0].scoreAndType == "1_$SEGMENT_TYPE_INNER_SINGLE"
                && combination[1].scoreAndType == "2_$SEGMENT_TYPE_INNER_SINGLE"
                && combination[2].scoreAndType == "3_$SEGMENT_TYPE_INNER_SINGLE"
    }

    private fun isValidSoFar(dartsSoFar: List<Dart>): Boolean
    {
        dartsSoFar.forEachIndexed { ix, drt ->
            if (drt.score != ix + 1 || drt.segmentType != SEGMENT_TYPE_INNER_SINGLE)
            {
                return false
            }
        }

        return true
    }
}

fun Dartboard.getFakeValidSegment(dartsThrown: Int) = when (dartsThrown) {
    0 -> getSegment(1, SEGMENT_TYPE_INNER_SINGLE)!!
    1 -> getSegment(2, SEGMENT_TYPE_INNER_SINGLE)!!
    else -> getSegment(3, SEGMENT_TYPE_INNER_SINGLE)!!
}