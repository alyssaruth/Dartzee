package dartzee.dartzee.aggregate

import dartzee.`object`.DartboardSegment
import dartzee.dartzee.AbstractDartzeeRule

abstract class AbstractDartzeeAggregateRule: AbstractDartzeeRule()
{
    abstract fun isValidRound(segments: List<DartboardSegment>): Boolean
}