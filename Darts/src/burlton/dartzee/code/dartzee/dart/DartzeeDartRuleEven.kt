package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.AbstractDartzeeRule

class DartzeeDartRuleEven : AbstractDartzeeRule(), IDartzeeDartRule
{
    override fun isValidSegment(segment: DartboardSegment): Boolean
    {
        return segment.score % 2 == 0 && !segment.isMiss()
    }

    override fun getRuleIdentifier() = "Even"
}