package dartzee.dartzee.dart

import dartzee.`object`.DartboardSegment

class DartzeeDartRuleAny: AbstractDartzeeDartRule()
{
    override fun getRuleIdentifier() = "Any"
    override fun isValidSegment(segment: DartboardSegment) = true
}