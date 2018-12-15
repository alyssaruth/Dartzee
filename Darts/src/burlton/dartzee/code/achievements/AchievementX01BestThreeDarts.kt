package burlton.dartzee.code.achievements

import burlton.dartzee.code.screen.stats.overall.OverallStatsScreen
import burlton.dartzee.code.utils.ResourceCache
import java.net.URL

class AchievementX01BestThreeDarts : AbstractAchievement()
{
    override val name = "Three Darter"
    override val desc = "Best three dart score in X01"
    override val achievementRef = ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE

    override val redThreshold = 60
    override val orangeThreshold = 80
    override val yellowThreshold = 100
    override val greenThreshold = 121
    override val blueThreshold = 141
    override val pinkThreshold = 171
    override val maxValue = 180

    override fun populateForConversion(playerIds: String)
    {
        unlockThreeDartAchievement(playerIds, "drtLast.DtCreation", "drtLast.Ordinal = 3", OverallStatsScreen.TOTAL_ROUND_SCORE_SQL_STR, achievementRef)
    }

    override fun getIconURL(): URL?
    {
        return ResourceCache.URL_ACHIEVEMENT_BEST_SCORE
    }
}