package burlton.dartzee.test.helper

import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRuleCalculationResult
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleColour
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleScore
import burlton.dartzee.code.dartzee.getAllTotalRules
import burlton.dartzee.code.dartzee.total.AbstractDartzeeRuleTotalSize
import burlton.dartzee.code.dartzee.total.AbstractDartzeeTotalRule

fun makeDartzeeRuleDto(dart1Rule: AbstractDartzeeDartRule? = null,
                       dart2Rule: AbstractDartzeeDartRule? = null,
                       dart3Rule: AbstractDartzeeDartRule? = null,
                       totalRule: AbstractDartzeeTotalRule? = null,
                       inOrder: Boolean = false,
                       allowMisses: Boolean = false,
                       calculationResult: DartzeeRuleCalculationResult = makeDartzeeRuleCalculationResult()): DartzeeRuleDto
{
    val rule = DartzeeRuleDto(dart1Rule, dart2Rule, dart3Rule, totalRule, inOrder, allowMisses)
    rule.calculationResult = calculationResult
    return rule
}

fun makeDartzeeRuleCalculationResult(validSegments: List<DartboardSegment> = listOf(),
                                     validCombinations: Int = 10,
                                     allCombinations: Int = 50,
                                     validCombinationProbability: Double = 1.0,
                                     allCombinationsProbability: Double = 6.0): DartzeeRuleCalculationResult
{
    return DartzeeRuleCalculationResult(validSegments, validCombinations, allCombinations, validCombinationProbability, allCombinationsProbability)
}

fun makeScoreRule(score: Int) = DartzeeDartRuleScore().also { it.score = score }
fun makeColourRule(red: Boolean = false, green: Boolean = false, black: Boolean = false, white: Boolean = false): DartzeeDartRuleColour
{
    val rule = DartzeeDartRuleColour()
    rule.black = black
    rule.white = white
    rule.red = red
    rule.green = green
    return rule
}

inline fun <reified T: AbstractDartzeeRuleTotalSize> makeTotalScoreRule(score: Int) = getAllTotalRules().find { it is T }.also { (it as T).target = score }