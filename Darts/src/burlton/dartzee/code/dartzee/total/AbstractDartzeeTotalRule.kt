package burlton.dartzee.code.dartzee.total

import burlton.dartzee.code.dartzee.AbstractDartzeeRule

abstract class AbstractDartzeeTotalRule: AbstractDartzeeRule()
{
    abstract fun isValidTotal(total: Int): Boolean
}