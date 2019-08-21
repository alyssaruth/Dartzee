package burlton.dartzee.code.dartzee.total

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment

interface IDartzeeTotalRule
{
    fun isValidTotal(total: Int): Boolean

    fun isValidSegment(segment: DartboardSegment, dartsSoFar: List<Dart>): Boolean
    {
        val total = dartsSoFar.map { it.getTotal() }.sum() + segment.getTotal()
        return isValidTotal(total)
    }
}