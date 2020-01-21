package dartzee.dartzee.dart

import dartzee.`object`.DartboardSegment
import dartzee.`object`.SEGMENT_TYPE_DOUBLE
import dartzee.`object`.SEGMENT_TYPE_OUTER_SINGLE

class DartzeeDartRuleOuter : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegment): Boolean
    {
        if (segment.score == 25)
        {
            return false
        }

        return segment.type == SEGMENT_TYPE_OUTER_SINGLE || segment.type == SEGMENT_TYPE_DOUBLE
    }

    override fun getRuleIdentifier() = "Outer"
}