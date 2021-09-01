package dartzee.dartzee.aggregate

import dartzee.`object`.DartboardSegment

abstract class AbstractDartzeeTotalRule: AbstractDartzeeAggregateRule()
{
    override fun isValidRound(segments: List<DartboardSegment>) = isValidTotal(segments.sumBy { it.getTotal() })
    abstract fun isValidTotal(total: Int): Boolean
}