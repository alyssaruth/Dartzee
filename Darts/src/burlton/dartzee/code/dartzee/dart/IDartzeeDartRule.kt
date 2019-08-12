package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegmentKt

interface IDartzeeDartRule
{
    fun isValidSegment(segment: DartboardSegmentKt): Boolean
}