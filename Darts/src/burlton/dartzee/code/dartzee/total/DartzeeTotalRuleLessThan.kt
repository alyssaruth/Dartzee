package burlton.dartzee.code.dartzee.total

class DartzeeTotalRuleLessThan: AbstractDartzeeRuleTotalSize()
{
    override fun getRuleIdentifier() = "LessThan"

    override fun isValidTotal(total: Int) = total < target
}