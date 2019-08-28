package burlton.dartzee.code.dartzee.total

class DartzeeTotalRuleEven: AbstractDartzeeTotalRule()
{
    override fun getRuleIdentifier() = "Even"

    override fun isValidTotal(total: Int) = (total % 2) == 0
    override fun getDescription() = "is even"

    override fun isPotentiallyValidTotal(total: Int, dartsRemaining: Int) = true
}