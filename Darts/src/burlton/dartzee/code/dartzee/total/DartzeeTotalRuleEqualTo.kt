package burlton.dartzee.code.dartzee.total

class DartzeeTotalRuleEqualTo: AbstractDartzeeRuleTotalSize()
{
    override fun getRuleIdentifier() = "EqualTo"

    override fun isValidTotal(total: Int) = total == target
    override fun isPotentiallyValidTotal(total: Int, dartsRemaining: Int): Boolean
    {
        return total + dartsRemaining <= target
                && total >= target - (60 * dartsRemaining)
    }

    override fun toString() = "Equal to"

    override fun getDescription() = "= $target"
}