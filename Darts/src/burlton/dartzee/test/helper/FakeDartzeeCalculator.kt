package burlton.dartzee.test.helper

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.`object`.SEGMENT_TYPE_INNER_SINGLE
import burlton.dartzee.code.dartzee.AbstractDartzeeCalculator
import burlton.dartzee.code.dartzee.DartzeeRuleCalculationResult
import burlton.dartzee.code.dartzee.DartzeeRuleDto

/**
 * Fast calculator for tests which assumes that the rule is always:
 *
 *  Inner single 1 -> Inner single 2 -> Inner single 3
 */
class FakeDartzeeCalculator: AbstractDartzeeCalculator()
{
    override fun getValidSegments(rule: DartzeeRuleDto, dartsSoFar: List<Dart>): DartzeeRuleCalculationResult
    {
        val segments = mutableListOf<DartboardSegment>()
        if (isValidSoFar(dartsSoFar))
        {
            val nextSegment = getFakeValidSegment(dartsSoFar.size)
            segments.add(nextSegment)
        }

        if (rule.allowMisses)
        {
            segments.add(DartboardSegment("20_0"))
        }

        return DartzeeRuleCalculationResult(segments, segments.size, 10, 1.0 + dartsSoFar.size, 10.0)
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

fun getFakeValidSegment(dartsThrown: Int) = when (dartsThrown) {
    0 -> DartboardSegment("1_$SEGMENT_TYPE_INNER_SINGLE")
    1 -> DartboardSegment("2_$SEGMENT_TYPE_INNER_SINGLE")
    else -> DartboardSegment("3_$SEGMENT_TYPE_INNER_SINGLE")
}