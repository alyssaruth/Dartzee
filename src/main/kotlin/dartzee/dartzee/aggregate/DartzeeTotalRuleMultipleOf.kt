package dartzee.dartzee.aggregate

class DartzeeTotalRuleMultipleOf: AbstractDartzeeRuleTotalSize()
{
    override fun getRuleIdentifier() = "MultipleOf"

    override fun isValidTotal(total: Int) = total % target == 0

    override fun toString() = "Total multiple of"

    override fun getDescription() = "Total multiple of $target"
}