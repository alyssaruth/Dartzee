package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegment

class DartzeeDartRuleAny: AbstractDartzeeDartRule()
{
    override fun getRuleIdentifier() = "Any"
    override fun isValidSegment(segment: DartboardSegment) = true
}