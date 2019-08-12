package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.dartzee.AbstractDartzeeRule

class DartzeeDartRuleEven : AbstractDartzeeRule(), IDartzeeDartRule
{
    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return segment.score % 2 == 0 && !segment.isMiss()
    }

    override fun getRuleIdentifier() = "Even"
}