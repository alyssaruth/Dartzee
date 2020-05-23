package dartzee.dartzee.dart

import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType

class DartzeeDartRuleInner : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegment): Boolean
    {
        return segment.type == SegmentType.INNER_SINGLE
            || segment.type == SegmentType.TREBLE
            || segment.score == 25
    }

    override fun getRuleIdentifier() = "Inner"
}