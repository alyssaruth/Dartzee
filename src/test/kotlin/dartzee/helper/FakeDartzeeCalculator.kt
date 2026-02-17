package dartzee.helper

import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.dartzee.IDartzeeCalculator
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType

/**
 * Fast calculator for tests which assumes that the rule is always:
 *
 * Inner single 1 -> Inner single 2 -> Inner single 3
 */
class FakeDartzeeCalculator : IDartzeeCalculator {
    override fun getValidSegments(
        rule: DartzeeRuleDto,
        dartsSoFar: List<Dart>,
    ): DartzeeRuleCalculationResult {
        val segments = mutableListOf<DartboardSegment>()
        if (isValidSoFar(dartsSoFar)) {
            val nextSegment = getFakeValidSegment(dartsSoFar.size)
            segments.add(nextSegment)
        }

        if (rule.allowMisses) {
            segments.add(DartboardSegment(SegmentType.MISS, 20))
        }

        return DartzeeRuleCalculationResult(
            segments,
            segments,
            segments.size,
            10,
            1.0 + dartsSoFar.size,
            10.0,
        )
    }

    private fun isValidSoFar(dartsSoFar: List<Dart>): Boolean {
        dartsSoFar.forEachIndexed { ix, drt ->
            if (drt.score != ix + 1 || drt.segmentType != SegmentType.INNER_SINGLE) {
                return false
            }
        }

        return true
    }
}

fun getFakeValidSegment(dartsThrown: Int) =
    when (dartsThrown) {
        0 -> DartboardSegment(SegmentType.INNER_SINGLE, 1)
        1 -> DartboardSegment(SegmentType.INNER_SINGLE, 2)
        else -> DartboardSegment(SegmentType.INNER_SINGLE, 3)
    }
