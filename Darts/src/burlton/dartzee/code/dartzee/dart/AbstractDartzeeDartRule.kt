package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.AbstractDartzeeRule

abstract class AbstractDartzeeDartRule: AbstractDartzeeRule()
{
    abstract fun isValidSegment(segment: DartboardSegment): Boolean
}