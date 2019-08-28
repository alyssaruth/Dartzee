package burlton.dartzee.code.dartzee.total

class DartzeeTotalRuleGreaterThan: AbstractDartzeeRuleTotalSize()
{
    override fun getRuleIdentifier() = "GreaterThan"

    override fun isValidTotal(total: Int) = total > target

    override fun toString() = "Greater than"

    override fun getDescription() = "> $target"

    override fun isPotentiallyValidTotal(total: Int, dartsRemaining: Int): Boolean
    {
        return total > target - (60 * dartsRemaining)
    }
}