package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.`object`.SEGMENT_TYPE_INNER_SINGLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE

class DartzeeDartRuleInner : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegment): Boolean
    {
        return segment.type == SEGMENT_TYPE_INNER_SINGLE
            || segment.type == SEGMENT_TYPE_TREBLE
            || segment.score == 25
    }

    override fun getRuleIdentifier() = "Inner"
}