package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.`object`.SEGMENT_TYPE_INNER_SINGLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE
import burlton.dartzee.code.dartzee.AbstractDartzeeRule

class DartzeeDartRuleInner : AbstractDartzeeRule(), IDartzeeDartRule
{
    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return segment.type == SEGMENT_TYPE_INNER_SINGLE
            || segment.type == SEGMENT_TYPE_TREBLE
            || segment.score == 25
    }

    override fun getRuleIdentifier() = "Inner"
}