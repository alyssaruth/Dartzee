package burlton.dartzee.code.dartzee

import burlton.dartzee.code.core.util.Debug
import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleCustom
import kotlin.random.Random

object DartzeeRandomiser
{
    data class DartRule(val dart1Rule: AbstractDartzeeDartRule, val dart2Rule: AbstractDartzeeDartRule?, val dart3Rule: AbstractDartzeeDartRule?, val inOrder: Boolean)

    fun generateRandomRule(): DartzeeRuleDto
    {
        val dartRuleBit = Random.nextInt(2)
        val totalRuleBit = Random.nextInt(2)

        val hasDartRule = dartRuleBit == 1 || totalRuleBit == 0
        val hasTotalRule = totalRuleBit == 1

        val dartRule = if (hasDartRule) makeDartRule() else null
        val totalRule = if (hasTotalRule) getAllTotalRules().random().also { it.randomise() } else null

        val allowMisses = Random.nextInt(5) == 1
        Debug.append("$dartRule, $totalRule, $allowMisses")
        return DartzeeRuleDto(dartRule?.dart1Rule, dartRule?.dart2Rule, dartRule?.dart3Rule, totalRule, dartRule?.inOrder ?: false, allowMisses)
    }

    private fun makeDartRule(): DartRule
    {
        val typeBit = Random.nextInt(3)
        return if (typeBit == 2)
        {
            DartRule(randomDartRule(), null, null, false)
        }
        else
        {
            // Generate all of them
            val inOrder = Random.nextInt(2) == 1
            DartRule(randomDartRule(), randomDartRule(), randomDartRule(), inOrder)
        }
    }

    private fun randomDartRule(): AbstractDartzeeDartRule
    {
        val rules = getAllDartRules().filter{ it !is DartzeeDartRuleCustom }
        return rules.random().also { it.randomise() }
    }
}