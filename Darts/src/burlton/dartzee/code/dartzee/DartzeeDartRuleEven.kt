package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.DartboardSegmentKt

class DartzeeDartRuleEven : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return segment.score % 2 == 0 && !segment.isMiss()
    }

    override fun getRuleIdentifier() = "Even"
}