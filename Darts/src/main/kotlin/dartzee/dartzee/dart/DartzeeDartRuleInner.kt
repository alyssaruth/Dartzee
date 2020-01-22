package dartzee.dartzee.dart

import dartzee.`object`.DartboardSegment
import dartzee.`object`.SEGMENT_TYPE_INNER_SINGLE
import dartzee.`object`.SEGMENT_TYPE_TREBLE

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