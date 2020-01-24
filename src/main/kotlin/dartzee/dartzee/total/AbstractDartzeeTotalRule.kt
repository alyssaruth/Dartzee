package dartzee.dartzee.total

import dartzee.dartzee.AbstractDartzeeRule

abstract class AbstractDartzeeTotalRule: AbstractDartzeeRule()
{
    abstract fun isValidTotal(total: Int): Boolean
}