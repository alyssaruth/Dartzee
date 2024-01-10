package dartzee.dartzee.dart

import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType

class DartzeeDartRuleOuter : AbstractDartzeeDartRule() {
    override fun isValidSegment(segment: DartboardSegment): Boolean {
        if (segment.score == 25) {
            return false
        }

        return segment.type == SegmentType.OUTER_SINGLE || segment.type == SegmentType.DOUBLE
    }

    override fun getRuleIdentifier() = "Outer"
}
