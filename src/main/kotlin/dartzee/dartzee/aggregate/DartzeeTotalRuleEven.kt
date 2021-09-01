package dartzee.dartzee.aggregate

class DartzeeTotalRuleEven: AbstractDartzeeTotalRule()
{
    override fun getRuleIdentifier() = "Even"

    override fun isValidTotal(total: Int) = (total % 2) == 0
    override fun getDescription() = "Total is even"
}