package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.`object`.SEGMENT_TYPE_MISS
import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.total.AbstractDartzeeTotalRule
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.screen.Dartboard

/**
 * Rule Descriptions
 */
fun DartzeeRuleEntity.generateRuleDescription(): String
{
    val dartsDesc = getDartsDescription()
    val totalDesc = getTotalDescription()

    val ruleParts = mutableListOf<String>()
    if (!dartsDesc.isEmpty()) ruleParts.add(dartsDesc)
    if (!totalDesc.isEmpty()) ruleParts.add(totalDesc)

    return ruleParts.joinToString()
}
private fun DartzeeRuleEntity.getTotalDescription(): String
{
    if (totalRule == "")
    {
        return ""
    }

    val desc = parseTotalRule(totalRule)!!.getDescription()

    return "Total $desc"
}
private fun DartzeeRuleEntity.getDartsDescription(): String
{
    if (dart1Rule == "")
    {
        return ""
    }

    if (dart2Rule == "")
    {
        return "Score ${parseDartRule(dart1Rule)!!.getDescription()}"
    }

    //It's a 3 dart rule
    val dart1Desc = parseDartRule(dart1Rule)!!.getDescription()
    val dart2Desc = parseDartRule(dart2Rule)!!.getDescription()
    val dart3Desc = parseDartRule(dart3Rule)!!.getDescription()

    return if (inOrder)
    {
        "$dart1Desc → $dart2Desc → $dart3Desc"
    }
    else
    {
        //Try to condense the descriptions
        val rules = listOf(dart1Desc, dart2Desc, dart3Desc)
        val mapEntries = rules.groupBy { it }.map { it }
        val sortedGroupedRules = mapEntries.sortedByDescending { it.value.size }.map { "${it.value.size}x ${it.key}" }
        "{ ${sortedGroupedRules.joinToString()} }"
    }
}

/**
 * Validation
 */
fun DartzeeRuleEntity.getValidSegments(dartboard: Dartboard, dartsSoFar: List<Dart>): List<DartboardSegment>
{
    val allPossibilities = generateAllPossibilities(dartboard, dartsSoFar, allowMisses)

    val dartRules = getParsedDartRules()
    val totalRule = parseTotalRule(totalRule)

    val validCombinations = allPossibilities.filter { isValidCombination(it, dartRules, totalRule) }

    return validCombinations.map { it[dartsSoFar.size] }.distinct()
}
fun DartzeeRuleEntity.isValidCombination(combination: List<DartboardSegment>,
                                         dartRules: List<AbstractDartzeeDartRule>?,
                                         totalRule: AbstractDartzeeTotalRule?): Boolean
{
    return isValidCombinationForTotalRule(combination, totalRule)
            && isValidCombinationForDartRule(combination, dartRules)
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
private fun DartzeeRuleEntity.isValidCombinationForDartRule(combination: List<DartboardSegment>, dartRules: List<AbstractDartzeeDartRule>?): Boolean
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