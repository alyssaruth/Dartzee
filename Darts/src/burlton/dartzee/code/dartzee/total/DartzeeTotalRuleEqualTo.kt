package burlton.dartzee.code.dartzee.total

class DartzeeTotalRuleEqualTo: AbstractDartzeeRuleTotalSize()
{
    override fun getRuleIdentifier() = "EqualTo"

    override fun isValidTotal(total: Int) = total == target

    override fun toString() = "Equal to"
}