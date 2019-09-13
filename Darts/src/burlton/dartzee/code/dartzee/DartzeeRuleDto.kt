package burlton.dartzee.code.dartzee

import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.total.AbstractDartzeeTotalRule
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.utils.InjectedThings.dartzeeCalculator

data class DartzeeRuleDto(val dart1Rule: AbstractDartzeeDartRule?, val dart2Rule: AbstractDartzeeDartRule?, val dart3Rule: AbstractDartzeeDartRule?,
                          val totalRule: AbstractDartzeeTotalRule?, val inOrder: Boolean, val allowMisses: Boolean)
{
    var calculationResult: ValidSegmentCalculationResult? = null

    fun getDartRuleList(): List<AbstractDartzeeDartRule>?
    {
        dart1Rule ?: return null
        dart2Rule ?: return listOf(dart1Rule)

        return listOf(dart1Rule, dart2Rule, dart3Rule!!)
    }

    fun runStrengthCalculation(dartboard: Dartboard): ValidSegmentCalculationResult
    {
        val calculationResult = dartzeeCalculator.getValidSegments(this, dartboard, listOf())

        this.calculationResult = calculationResult

        return calculationResult
    }

    fun getStrengthDesc() = calculationResult?.getCombinationsDesc() ?: ""

    fun generateRuleDescription(): String
    {
        val dartsDesc = getDartsDescription()
        val totalDesc = getTotalDescription()

        val ruleParts = mutableListOf<String>()
        if (!dartsDesc.isEmpty()) ruleParts.add(dartsDesc)
        if (!totalDesc.isEmpty()) ruleParts.add(totalDesc)

        val result = ruleParts.joinToString()
        return if (result.isEmpty()) "Anything" else result
    }
    private fun getTotalDescription(): String
    {
        totalRule ?: return ""

        return "Total ${totalRule.getDescription()}"
    }
    private fun getDartsDescription(): String
    {
        dart1Rule ?: return ""
        dart2Rule ?: return "Score ${dart1Rule.getDescription()}"

        //It's a 3 dart rule
        val dart1Desc = dart1Rule.getDescription()
        val dart2Desc = dart2Rule.getDescription()
        val dart3Desc = dart3Rule!!.getDescription()

        val rules = listOf(dart1Desc, dart2Desc, dart3Desc)
        if (rules.all { it == "Any"} )
        {
            return ""
        }

        return if (inOrder)
        {
            "$dart1Desc → $dart2Desc → $dart3Desc"
        }
        else
        {
            //Try to condense the descriptions
            val interestingRules = rules.filter { it != "Any" }

            val mapEntries = interestingRules.groupBy { it }.map { it }
            val sortedGroupedRules = mapEntries.sortedByDescending { it.value.size }.map { "${it.value.size}x ${it.key}" }
            "{ ${sortedGroupedRules.joinToString()} }"
        }
    }
}