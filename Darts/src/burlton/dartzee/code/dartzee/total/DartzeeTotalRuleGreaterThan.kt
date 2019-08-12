package burlton.dartzee.code.dartzee.total

class DartzeeTotalRuleGreaterThan: AbstractDartzeeRuleTotalSize()
{
    override fun getRuleIdentifier() = "GreaterThan"

    override fun isValidTotal(total: Int) = total > target
}