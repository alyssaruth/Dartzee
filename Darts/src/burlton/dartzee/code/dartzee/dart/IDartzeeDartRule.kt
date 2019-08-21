package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegment

interface IDartzeeDartRule
{
    fun isValidSegment(segment: DartboardSegment): Boolean
}