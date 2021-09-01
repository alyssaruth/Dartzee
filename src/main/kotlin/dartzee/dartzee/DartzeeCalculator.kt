package dartzee.dartzee

import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.core.util.allIndexed
import dartzee.core.util.getAllPermutations
import dartzee.dartzee.aggregate.AbstractDartzeeAggregateRule
import dartzee.dartzee.dart.AbstractDartzeeDartRule
import dartzee.utils.getAllNonMissSegments

abstract class AbstractDartzeeCalculator
{
    abstract fun getValidSegments(rule: DartzeeRuleDto, dartsSoFar: List<Dart>): DartzeeRuleCalculationResult
}

class DartzeeCalculator: AbstractDartzeeCalculator()
{
    private val allPossibilities: List<List<DartboardSegment>> = generateAllPossibilities()

    private fun isValidDartCombination(darts: List<Dart>, rule: DartzeeRuleDto) =
            isValidCombination(darts.map { DartboardSegment(it.segmentType, it.score) }, rule)

    fun isValidCombination(combination: List<DartboardSegment>,
                           rule: DartzeeRuleDto,
                           cachedResults: MutableMap<List<DartboardSegment>, Boolean> = mutableMapOf()): Boolean
    {
        return isValidCombinationForAggregateRule(combination, rule.aggregateRule)
                && isValidFromMisses(combination, rule)
                && isValidCombinationForDartRule(combination, rule.getDartRuleList(), rule.inOrder, cachedResults)
    }

    override fun getValidSegments(rule: DartzeeRuleDto, dartsSoFar: List<Dart>): DartzeeRuleCalculationResult
    {
        if (dartsSoFar.size == 3)
        {
            val valid = isValidDartCombination(dartsSoFar, rule)
            return if (valid) getValidSegments(rule, listOf(dartsSoFar[0], dartsSoFar[1])) else INVALID_CALCULATION_RESULT
        }

        val cachedCombinationResults = mutableMapOf<List<DartboardSegment>, Boolean>()

        val allPossibilities = generateAllPossibilities(dartsSoFar)
        val validCombinations = allPossibilities.filter {
            isValidCombination(it, rule, cachedCombinationResults) }

        val validSegments = validCombinations.map { it[dartsSoFar.size] }.distinct()
        val scoringSegments = rule.getScoringSegments(validSegments)

        val validPixelPossibility = validCombinations.map { mapCombinationToProbability(it) }.sum()
        val allProbabilities = allPossibilities.map { mapCombinationToProbability(it) }.sum()

        return DartzeeRuleCalculationResult(scoringSegments,
            validSegments,
            validCombinations.size,
            allPossibilities.size,
            validPixelPossibility,
            allProbabilities)
    }
    private fun isValidFromMisses(combination: List<DartboardSegment>, rule: DartzeeRuleDto): Boolean
    {
        return rule.allowMisses || combination.all { !it.isMiss() }
    }

    private fun isValidCombinationForAggregateRule(combination: List<DartboardSegment>, aggregateRule: AbstractDartzeeAggregateRule?): Boolean
    {
        if (aggregateRule == null)
        {
            return true
        }

        return aggregateRule.isValidRound(combination)
    }
    private fun isValidCombinationForDartRule(combination: List<DartboardSegment>,
                                              dartRules: List<AbstractDartzeeDartRule>?,
                                              inOrder: Boolean,
                                              cachedResults: MutableMap<List<DartboardSegment>, Boolean> = mutableMapOf()): Boolean
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
            if (cachedResults.containsKey(combination))
            {
                cachedResults[combination]!!
            }
            else
            {
                val permutations = combination.getAllPermutations()

                val valid = permutations.any { isValidCombinationForOrderedDartRule(dartRules, it) }

                permutations.forEach { cachedResults[it] = valid }

                valid
            }
        }
    }
    private fun isValidCombinationForOrderedDartRule(rules: List<AbstractDartzeeDartRule>, combination: List<DartboardSegment>): Boolean
    {
        return rules.allIndexed { ix, rule -> rule.isValidSegment(combination[ix]) }
    }

    private fun mapCombinationToProbability(combination: List<DartboardSegment>): Double
    {
        val probabilities = combination.map { it.getRoughProbability() }
        return probabilities.reduce { acc, i -> acc * i }
    }

    private fun getAllSegments(): List<DartboardSegment>
    {
        val segments = getAllNonMissSegments()
        return segments + DartboardSegment(SegmentType.MISS, 20)
    }

    private fun generateAllPossibilities(): List<List<DartboardSegment>>
    {
        val segments = getAllSegments()

        val allPossibilities: MutableList<List<DartboardSegment>> = mutableListOf()
        segments.forEach { s1 ->
            segments.forEach { s2 ->
                segments.forEach { s3 ->
                    val combination = listOf(s1, s2, s3)
                    allPossibilities.add(combination)
                }
            }
        }

        return allPossibilities.toList()
    }

    fun generateAllPossibilities(dartsSoFar: List<Dart>): List<List<DartboardSegment>>
    {
        if (dartsSoFar.isEmpty())
        {
            return allPossibilities
        }

        val segments = getAllSegments()
        val segmentsSoFar = dartsSoFar.map { DartboardSegment(it.segmentType, it.score) }

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
