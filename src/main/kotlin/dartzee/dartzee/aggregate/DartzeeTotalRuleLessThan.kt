package dartzee.dartzee.aggregate

class DartzeeTotalRuleLessThan: AbstractDartzeeRuleTotalSize()
{
    override fun getRuleIdentifier() = "LessThan"

    override fun isValidTotal(total: Int) = total < target

    override fun toString() = "Total less than"

    override fun getDescription() = "Total < $target"
}