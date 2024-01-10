package dartzee.dartzee.aggregate

class DartzeeTotalRuleOdd : AbstractDartzeeTotalRule() {
    override fun getRuleIdentifier() = "Odd"

    override fun isValidTotal(total: Int) = (total % 2) != 0

    override fun toString() = "Total is odd"
}
