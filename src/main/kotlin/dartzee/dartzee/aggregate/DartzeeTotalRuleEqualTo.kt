package dartzee.dartzee.aggregate

class DartzeeTotalRuleEqualTo: AbstractDartzeeRuleTotalSize()
{
    override fun getRuleIdentifier() = "EqualTo"

    override fun isValidTotal(total: Int) = total == target

    override fun toString() = "Total equal to"

    override fun getDescription() = "= $target"
}