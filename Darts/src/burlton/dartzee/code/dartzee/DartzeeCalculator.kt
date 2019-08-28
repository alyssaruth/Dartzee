package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRule
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.screen.Dartboard

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

fun DartzeeRuleEntity.getValidSegments(dartboard: Dartboard, dartsSoFar: List<Dart>): List<DartboardSegment>
{
    val segments = dartboard.getAllSegments()
    return segments.filter { isValidSegment(it, dartsSoFar) }
}
fun DartzeeRuleEntity.isValidSegment(segment: DartboardSegment, dartsSoFar: List<Dart>): Boolean
{
    val exampleDart = segment.getExampleDart()
    return isValidSegmentForDartsRules(segment, dartsSoFar)
            && isValidDartForTotalRule(exampleDart, dartsSoFar)
}
private fun DartzeeRuleEntity.isValidSegmentForDartsRules(segment: DartboardSegment, dartsSoFar: List<Dart>): Boolean
{
    val parsedRule1 = parseDartRule(dart1Rule) ?: return true
    val parsedRule2 = parseDartRule(dart2Rule)

    //This is an "at least one" rule, so just need any of the previous darts or this one to be valid
    if (parsedRule2 == null)
    {
        val exampleDart = segment.getExampleDart()
        val allDarts = dartsSoFar + exampleDart
        return allDarts.any { parsedRule1.isValidSegment(segment) }
    }

    val parsedRule3 = parseDartRule(dart3Rule)!!
    val allRules = listOf(parsedRule1, parsedRule2, parsedRule3)
    if (inOrder)
    {
        //Need to compare to the precise rule for this dart
        return isValidDartForOrderedDartRule(segment, dartsSoFar, allRules)
    }
    else
    {
        return isValidDartForAnyOrderDartsRule(segment, dartsSoFar, allRules)
    }
}
private fun isValidDartForOrderedDartRule(segment: DartboardSegment, dartsSoFar: List<Dart>, rules: List<AbstractDartzeeDartRule>): Boolean
{
    var valid = true
    val exampleDart = segment.getExampleDart()
    val allDarts = dartsSoFar + exampleDart

    allDarts.forEachIndexed{ i, drt ->
        valid = valid && rules[i].isValidDart(drt)
    }

    return valid
}
private fun isValidDartForAnyOrderDartsRule(segment: DartboardSegment, dartsSoFar: List<Dart>, rules: List<AbstractDartzeeDartRule>): Boolean
{
    return when (dartsSoFar.size)
    {
        0 -> rules.any { it.isValidSegment(segment) }
        1 -> false
        else -> getAllRulePermutations(rules).any { isValidDartForOrderedDartRule(segment, dartsSoFar, it) }
    }
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
private fun DartzeeRuleEntity.isValidDartForTotalRule(dart: Dart, dartsSoFar: List<Dart>): Boolean
{
    if (totalRule == "")
    {
        return true
    }

    val rule = parseTotalRule(totalRule)!!
    return rule.isValidDart(dart, dartsSoFar)
}