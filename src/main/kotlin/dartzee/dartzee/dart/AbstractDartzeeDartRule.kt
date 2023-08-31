package dartzee.dartzee.dart

import dartzee.dartzee.AbstractDartzeeRule
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment

abstract class AbstractDartzeeDartRule: AbstractDartzeeRule()
{
    abstract fun isValidSegment(segment: DartboardSegment): Boolean

    fun isValidDart(dart: Dart) = isValidSegment(DartboardSegment(dart.segmentType, dart.score))
}