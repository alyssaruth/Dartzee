package burlton.dartzee.code.dartzee.total

import burlton.dartzee.code.dartzee.AbstractDartzeeRule

class DartzeeTotalRuleEven: AbstractDartzeeRule(), IDartzeeTotalRule
{
    override fun getRuleIdentifier() = "Even"

    override fun isValidTotal(total: Int) = (total % 2) == 0
    override fun getDescription() = "is even"
}