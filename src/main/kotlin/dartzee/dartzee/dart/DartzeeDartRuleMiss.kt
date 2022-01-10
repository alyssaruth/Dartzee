package dartzee.dartzee.dart

import dartzee.`object`.DartboardSegment

class DartzeeDartRuleMiss: AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegment) = segment.isMiss()

    override fun getRuleIdentifier() = "Miss"
}