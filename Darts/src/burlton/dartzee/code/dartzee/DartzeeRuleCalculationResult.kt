package burlton.dartzee.code.dartzee

import burlton.core.code.util.MathsUtil
import burlton.dartzee.code.`object`.DartboardSegment

data class DartzeeRuleCalculationResult(val validSegments: List<DartboardSegment>,
                                        val validCombinations: Int,
                                        val allCombinations: Int,
                                        val validCombinationProbability: Double,
                                        val allCombinationsProbability: Double)
{
    val percentage = MathsUtil.getPercentage(validCombinationProbability, allCombinationsProbability)

    fun getCombinationsDesc() = "$validCombinations combinations (success%: $percentage%)"

    fun getDifficultyDesc() = when
    {
        percentage == 0.0 -> "Impossible"
        percentage > 40 -> "Very Easy"
        percentage > 25 -> "Easy"
        percentage > 10 -> "Moderate"
        percentage > 5 -> "Hard"
        percentage > 1 -> "Very Hard"
        else -> "Insane"
    }
}