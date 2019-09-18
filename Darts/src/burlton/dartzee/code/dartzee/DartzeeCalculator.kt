package burlton.dartzee.code.dartzee

import burlton.core.code.util.MathsUtil
import burlton.core.code.util.getAllPermutations
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.`object`.SEGMENT_TYPE_MISS
import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.total.AbstractDartzeeTotalRule
import burlton.dartzee.code.screen.Dartboard

data class DartzeeRuleCalculationResult(val validSegments: List<DartboardSegment>,
                                        val validCombinations: Int,
                                        val allCombinations: Int,
                                        val validCombinationProbability: Double,
                                        val allCombinationsProbability: Double)
{
    private val percentage = MathsUtil.getPercentage(validCombinationProbability, allCombinationsProbability)

    fun getCombinationsDesc() = "$validCombinations combinations (success%: $percentage%)"
}

abstract class AbstractDartzeeCalculator
{
    abstract fun getValidSegments(rule: DartzeeRuleDto, dartboard: Dartboard, dartsSoFar: List<Dart>): DartzeeRuleCalculationResult
    abstract fun isValidCombination(combination: List<DartboardSegment>, rule: DartzeeRuleDto): Boolean
}

class DartzeeCalculator: AbstractDartzeeCalculator()
{
    override fun getValidSegments(rule: DartzeeRuleDto, dartboard: Dartboard, dartsSoFar: List<Dart>): DartzeeRuleCalculationResult
    {
        val allPossibilities = generateAllPossibilities(dartboard, dartsSoFar, true)

        val validCombinations = allPossibilities.filter { isValidCombination(it, rule) }
                .filter { it.all { segment -> !segment.isMiss() || rule.allowMisses }}

        val validSegments = validCombinations.map { it[dartsSoFar.size] }.distinct()

        val validPixelPossibility = validCombinations.map { mapCombinationToProbability(it, dartboard) }.sum()
        val allProbabilities = allPossibilities.map { mapCombinationToProbability(it, dartboard) }.sum()

        return DartzeeRuleCalculationResult(validSegments, validCombinations.size, allPossibilities.size, validPixelPossibility, allProbabilities)
    }
    override fun isValidCombination(combination: List<DartboardSegment>,
                           rule: DartzeeRuleDto): Boolean
    {
        return isValidCombinationForTotalRule(combination, rule.totalRule)
                && isValidCombinationForDartRule(combination, rule.getDartRuleList(), rule.inOrder)
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
            dartRules.getAllPermutations().any { isValidCombinationForOrderedDartRule(it, combination) }
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
}
