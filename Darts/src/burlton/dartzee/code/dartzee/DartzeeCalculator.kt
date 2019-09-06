package burlton.dartzee.code.dartzee

import burlton.core.code.util.MathsUtil
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.`object`.SEGMENT_TYPE_MISS
import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.total.AbstractDartzeeTotalRule
import burlton.dartzee.code.screen.Dartboard

/**
 * Rule Descriptions
 */
fun DartzeeRuleDto.generateRuleDescription(): String
{
    val dartsDesc = getDartsDescription()
    val totalDesc = getTotalDescription()

    val ruleParts = mutableListOf<String>()
    if (!dartsDesc.isEmpty()) ruleParts.add(dartsDesc)
    if (!totalDesc.isEmpty()) ruleParts.add(totalDesc)

    val result = ruleParts.joinToString()
    return if (result.isEmpty()) "Anything" else result
}
private fun DartzeeRuleDto.getTotalDescription(): String
{
    totalRule ?: return ""

    return "Total ${totalRule.getDescription()}"
}
private fun DartzeeRuleDto.getDartsDescription(): String
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

data class ValidSegmentCalculationResult(val validSegments: List<DartboardSegment>,
                                         val validCombinations: Int,
                                         val allCombinations: Int,
                                         val validCombinationProbability: Double,
                                         val allCombinationsProbability: Double)
{
    private val percentage = MathsUtil.getPercentage(validCombinationProbability, allCombinationsProbability)

    fun getCombinationsDesc() = "$validCombinations combinations ($percentage%)"
}

/**
 * Validation
 */
fun DartzeeRuleDto.getValidSegments(dartboard: Dartboard, dartsSoFar: List<Dart>): ValidSegmentCalculationResult
{
    val allPossibilities = generateAllPossibilities(dartboard, dartsSoFar, true)

    val dartRules = getDartRuleList()
    val totalRule = totalRule

    val validCombinations = allPossibilities.filter { isValidCombination(it, dartRules, totalRule, inOrder) }
            .filter { it.all { segment -> !segment.isMiss() || allowMisses }}
    val validSegments = validCombinations.map { it[dartsSoFar.size] }.distinct()

    val validPixelPossibility = validCombinations.map { mapCombinationToProbability(it, dartboard) }.sum()
    val allProbabilities = allPossibilities.map { mapCombinationToProbability(it, dartboard) }.sum()

    return ValidSegmentCalculationResult(validSegments, validCombinations.size, allPossibilities.size, validPixelPossibility, allProbabilities)
}
fun isValidCombination(combination: List<DartboardSegment>,
                                         dartRules: List<AbstractDartzeeDartRule>?,
                                         totalRule: AbstractDartzeeTotalRule?,
                       inOrder: Boolean): Boolean
{
    return isValidCombinationForTotalRule(combination, totalRule)
            && isValidCombinationForDartRule(combination, dartRules, inOrder)
}
private fun isValidCombinationForTotalRule(combination: List<DartboardSegment>, totalRule: AbstractDartzeeTotalRule?): Boolean
{
    if (totalRule == null)
    {
        return true
    }

    val total = combination.map { it.score * it.getMultiplier() }.sum()
    return totalRule.isValidTotal(total)
}
private fun isValidCombinationForDartRule(combination: List<DartboardSegment>, dartRules: List<AbstractDartzeeDartRule>?, inOrder: Boolean): Boolean
{
    if (dartRules == null)
    {
        return true
    }

    if (dartRules.size == 1)
    {
        val rule = dartRules.first()
        return combination.any { rule.isValidSegment(it) }
    }

    return if (inOrder)
    {
        isValidCombinationForOrderedDartRule(dartRules, combination)
    }
    else
    {
        getAllRulePermutations(dartRules).any { isValidCombinationForOrderedDartRule(it, combination) }
    }
}
private fun isValidCombinationForOrderedDartRule(rules: List<AbstractDartzeeDartRule>, combination: List<DartboardSegment>): Boolean
{
    return rules.mapIndexed { ix, rule -> rule.isValidSegment(combination[ix]) }.all { it }
}

private fun mapCombinationToProbability(combination: List<DartboardSegment>, dartboard: Dartboard): Double
{
    val probabilities = combination.map { getProbabilityOfSegment(dartboard, it) }
    return probabilities.reduce { acc, i -> acc * i }
}

private fun getProbabilityOfSegment(dartboard: Dartboard, segment: DartboardSegment): Double
{
    val allPointsCount = dartboard.scoringPoints.size.toDouble()
    return dartboard.getPointsForSegment(segment.score, segment.type).size.toDouble() / allPointsCount
}

fun generateAllPossibilities(dartboard: Dartboard, dartsSoFar: List<Dart>, allowMisses: Boolean): List<List<DartboardSegment>>
{
    val segments = dartboard.getAllSegments().filter { !it.isMiss() }.toMutableList()
    if (allowMisses)
    {
        segments.add(DartboardSegment("20_$SEGMENT_TYPE_MISS"))
    }

    val segmentsSoFar = dartsSoFar.map { dartboard.getSegment(it.score, it.segmentType)!! }

    var allPossibilities: List<List<DartboardSegment>> = segments.map { segmentsSoFar + it }
    while (allPossibilities.first().size < 3)
    {
        allPossibilities = addAnotherLayer(allPossibilities, segments)
    }

    return allPossibilities
}
private fun addAnotherLayer(allPossibilities: List<List<DartboardSegment>>, segments: List<DartboardSegment>): List<List<DartboardSegment>>
{
    val ret = mutableListOf<List<DartboardSegment>>()
    for (possibility in allPossibilities)
    {
        segments.forEach {
            ret.add(possibility + it)
        }
    }

    return ret
}
private fun getAllRulePermutations(rules: List<AbstractDartzeeDartRule>): List<List<AbstractDartzeeDartRule>>
{
    return listOf(rules,
            listOf(rules[0], rules[2], rules[1]),
            listOf(rules[1], rules[0], rules[2]),
            listOf(rules[1], rules[2], rules[0]),
            listOf(rules[2], rules[0], rules[1]),
            listOf(rules[2], rules[1], rules[0]))
}