package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegment

class DartzeeDartRuleOdd : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegment) = segment.score % 2 != 0 && !segment.isMiss()

    override fun getRuleIdentifier() = "Odd"
}