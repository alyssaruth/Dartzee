package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
import burlton.dartzee.code.dartzee.AbstractDartzeeRule

class DartzeeDartRuleOuter : AbstractDartzeeRule(), IDartzeeDartRule
{
    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        if (segment.score == 25)
        {
            return false
        }

        return segment.type == SEGMENT_TYPE_OUTER_SINGLE || segment.type == SEGMENT_TYPE_DOUBLE
    }

    override fun getRuleIdentifier() = "Outer"
}