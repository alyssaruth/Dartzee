package dartzee.dartzee.aggregate

class DartzeeTotalRuleGreaterThan: AbstractDartzeeRuleTotalSize()
{
    override fun getRuleIdentifier() = "GreaterThan"

    override fun isValidTotal(total: Int) = total > target

    override fun toString() = "Total greater than"

    override fun getDescription() = "> $target"
}