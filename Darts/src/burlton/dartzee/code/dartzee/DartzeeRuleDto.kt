package burlton.dartzee.code.dartzee

import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.total.AbstractDartzeeTotalRule
import burlton.dartzee.code.screen.Dartboard

data class DartzeeRuleDto(val dart1Rule: AbstractDartzeeDartRule?, val dart2Rule: AbstractDartzeeDartRule?, val dart3Rule: AbstractDartzeeDartRule?,
                          val totalRule: AbstractDartzeeTotalRule?, val inOrder: Boolean, val allowMisses: Boolean)
{
    private var calculationResult: ValidSegmentCalculationResult? = null

    fun getDartRuleList(): List<AbstractDartzeeDartRule>?
    {
        dart1Rule ?: return null
        dart2Rule ?: return listOf(dart1Rule)

        return listOf(dart1Rule, dart2Rule, dart3Rule!!)
    }

    fun runStrengthCalculation(dartboard: Dartboard): ValidSegmentCalculationResult
    {
        val calculationResult = getValidSegments(dartboard, listOf())

        this.calculationResult = calculationResult

        return calculationResult
    }

    fun getStrengthDesc() = calculationResult?.getCombinationsDesc() ?: ""
}