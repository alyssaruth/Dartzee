package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegment

class DartzeeDartRuleEven : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegment): Boolean
    {
        return segment.score % 2 == 0 && !segment.isMiss()
    }

    override fun getRuleIdentifier() = "Even"
}