package dartzee.dartzee.total

import dartzee.`object`.DartboardSegment
import dartzee.dartzee.aggregate.AbstractDartzeeAggregateRule

abstract class AbstractDartzeeTotalRule: AbstractDartzeeAggregateRule()
{
    override fun isValidRound(segments: List<DartboardSegment>) = isValidTotal(segments.sumBy { it.getTotal() })
    abstract fun isValidTotal(total: Int): Boolean
}