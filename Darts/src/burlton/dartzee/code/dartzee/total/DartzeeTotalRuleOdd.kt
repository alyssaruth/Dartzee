package burlton.dartzee.code.dartzee.total

class DartzeeTotalRuleOdd: AbstractDartzeeTotalRule()
{
    override fun getRuleIdentifier() = "Odd"

    override fun isValidTotal(total: Int) = (total % 2) != 0
    override fun getDescription() = "is odd"
}