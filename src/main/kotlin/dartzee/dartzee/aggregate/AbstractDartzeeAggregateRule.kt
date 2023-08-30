package dartzee.dartzee.aggregate

import dartzee.dartzee.AbstractDartzeeRule
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment

abstract class AbstractDartzeeAggregateRule: AbstractDartzeeRule()
{
    abstract fun isValidRound(segments: List<DartboardSegment>): Boolean

    open fun getScoringDarts(darts: List<Dart>) = darts
}