package dartzee.dartzee

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import dartzee.core.util.MathsUtil
import dartzee.core.util.jsonMapper
import dartzee.`object`.DartboardSegment
import dartzee.screen.game.SegmentStatuses
import dartzee.utils.DartsColour
import kotlin.math.sqrt

enum class DartzeeRuleDifficulty(val desc: String) {
    IMPOSSIBLE("Impossible"),
    INSANE("Insane"),
    VERY_HARD("Very Hard"),
    HARD("Hard"),
    MODERATE("Moderate"),
    EASY("Easy"),
    VERY_EASY("Very Easy"),
}

val INVALID_CALCULATION_RESULT = DartzeeRuleCalculationResult(listOf(), listOf(), 0, 0, 0.0, 1.0)

@JsonIgnoreProperties(
    "percentage",
    "combinationsDesc",
    "difficultyDesc",
    "segmentStatus",
    "foreground",
    "background",
    "difficulty",
)
data class DartzeeRuleCalculationResult(
    val scoringSegments: List<DartboardSegment>,
    val validSegments: List<DartboardSegment>,
    val validCombinations: Int,
    val allCombinations: Int,
    val validCombinationProbability: Double,
    val allCombinationsProbability: Double,
) {
    val percentage =
        MathsUtil.getPercentage(validCombinationProbability, allCombinationsProbability)

    fun getCombinationsDesc() = "$validCombinations combinations (success%: $percentage%)"

    fun getDifficultyDesc() = getDifficulty().desc

    fun getSegmentStatus() = SegmentStatuses(scoringSegments, validSegments)

    fun getForeground() = DartsColour.getProportionalColourRedToGreen(sqrt(percentage), 10, 1.0)

    fun getBackground() = DartsColour.getProportionalColourRedToGreen(sqrt(percentage), 10, 0.5)

    private fun getDifficulty() =
        when {
            validCombinations == 0 -> DartzeeRuleDifficulty.IMPOSSIBLE
            percentage > 40 -> DartzeeRuleDifficulty.VERY_EASY
            percentage > 25 -> DartzeeRuleDifficulty.EASY
            percentage > 10 -> DartzeeRuleDifficulty.MODERATE
            percentage > 5 -> DartzeeRuleDifficulty.HARD
            percentage > 1 -> DartzeeRuleDifficulty.VERY_HARD
            else -> DartzeeRuleDifficulty.INSANE
        }

    fun toDbString(): String = jsonMapper().writeValueAsString(this)

    companion object {
        fun fromDbString(dbString: String): DartzeeRuleCalculationResult =
            jsonMapper().readValue(dbString)
    }
}
