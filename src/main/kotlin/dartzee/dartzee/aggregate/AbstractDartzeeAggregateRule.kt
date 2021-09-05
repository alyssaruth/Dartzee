package dartzee.dartzee.aggregate

import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.dartzee.AbstractDartzeeRule

abstract class AbstractDartzeeAggregateRule: AbstractDartzeeRule()
{
    abstract fun isValidRound(segments: List<DartboardSegment>): Boolean

    open fun getScoringDarts(darts: List<Dart>): List<Dart>
    {
        return darts
    }
}