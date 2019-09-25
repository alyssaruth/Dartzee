package burlton.dartzee.code.dartzee

import burlton.core.code.util.MathsUtil
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.utils.DartsColour
import java.awt.Color

enum class DartzeeRuleDifficulty(val desc: String, val color: Color)
{
    IMPOSSIBLE("Impossible", Color.BLACK),
    INSANE("Insane", DartsColour.getDarkenedColour(Color.RED)),
    VERY_HARD("Very Hard", Color.RED),
    HARD("Hard", DartsColour.COLOUR_ACHIEVEMENT_ORANGE),
    MODERATE("Moderate", Color.YELLOW.darker()),
    EASY("Easy", Color.GREEN),
    VERY_EASY("Very Easy", DartsColour.getDarkenedColour(Color.GREEN))
}

data class DartzeeRuleCalculationResult(val validSegments: List<DartboardSegment>,
                                        val validCombinations: Int,
                                        val allCombinations: Int,
                                        val validCombinationProbability: Double,
                                        val allCombinationsProbability: Double)
{
    val percentage = MathsUtil.getPercentage(validCombinationProbability, allCombinationsProbability)

    fun getCombinationsDesc() = "$validCombinations combinations (success%: $percentage%)"

    fun getDifficultyDesc() = getDifficulty().desc

    fun getForeground() = Color.WHITE
    fun getBackground() = getDifficulty().color

    fun getDifficulty() = when
    {
        validCombinations == 0 -> DartzeeRuleDifficulty.IMPOSSIBLE
        percentage > 40 -> DartzeeRuleDifficulty.VERY_EASY
        percentage > 25 -> DartzeeRuleDifficulty.EASY
        percentage > 10 -> DartzeeRuleDifficulty.MODERATE
        percentage > 5 -> DartzeeRuleDifficulty.HARD
        percentage > 1 -> DartzeeRuleDifficulty.VERY_HARD
        else -> DartzeeRuleDifficulty.INSANE
    }
}