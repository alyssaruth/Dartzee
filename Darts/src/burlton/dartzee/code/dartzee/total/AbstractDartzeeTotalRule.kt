package burlton.dartzee.code.dartzee.total

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.dartzee.AbstractDartzeeRule

abstract class AbstractDartzeeTotalRule: AbstractDartzeeRule()
{
    abstract fun isValidTotal(total: Int): Boolean
    abstract fun isPotentiallyValidTotal(total: Int, dartsRemaining: Int): Boolean

    fun isValidDart(dart: Dart, dartsSoFar: List<Dart>): Boolean
    {
        val total = dartsSoFar.map { it.getTotal() }.sum() + dart.getTotal()
        if (dartsSoFar.size == 2)
        {
            return isValidTotal(total)
        }
        else
        {
            return isPotentiallyValidTotal(total, 2 - dartsSoFar.size)
        }
    }
}