package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.DartboardSegmentKt

class DartzeeDartRuleOdd : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return segment.score % 2 != 0 && !segment.isMiss()
    }

    override fun getRuleIdentifier() = "Odd"
}