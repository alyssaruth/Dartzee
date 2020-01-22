package dartzee.dartzee.dart

import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.dartzee.AbstractDartzeeRule

abstract class AbstractDartzeeDartRule: AbstractDartzeeRule()
{
    abstract fun isValidSegment(segment: DartboardSegment): Boolean

    fun isValidDart(dart: Dart): Boolean
    {
        return isValidSegment(DartboardSegment("${dart.score}_${dart.segmentType}"))
    }
}