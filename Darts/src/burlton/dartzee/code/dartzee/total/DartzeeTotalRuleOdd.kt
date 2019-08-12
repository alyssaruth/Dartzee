package burlton.dartzee.code.dartzee.total

import burlton.dartzee.code.dartzee.AbstractDartzeeRule

class DartzeeTotalRuleOdd: AbstractDartzeeRule(), IDartzeeTotalRule
{
    override fun getRuleIdentifier() = "Odd"

    override fun isValidTotal(total: Int) = (total % 2) != 0
}