package dartzee.achievements.x01

import dartzee.achievements.ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE
import dartzee.achievements.AbstractAchievement
import dartzee.achievements.getNotBustSql
import dartzee.achievements.unlockThreeDartAchievement
import dartzee.db.GameType
import dartzee.utils.ResourceCache
import dartzee.utils.TOTAL_ROUND_SCORE_SQL_STR
import java.net.URL

class AchievementX01BestThreeDarts : AbstractAchievement()
{
    override val name = "Three Darter"
    override val desc = "Best three dart score in X01"
    override val achievementRef = ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE
    override val gameType = GameType.X01

    override val redThreshold = 60
    override val orangeThreshold = 80
    override val yellowThreshold = 100
    override val greenThreshold = 121
    override val blueThreshold = 141
    override val pinkThreshold = 171
    override val maxValue = 180

    override fun populateForConversion(playerIds: String)
    {
        val dartSql = "${getNotBustSql()} AND drtLast.Ordinal = 3"
        unlockThreeDartAchievement(playerIds, "drtLast.DtCreation", dartSql, TOTAL_ROUND_SCORE_SQL_STR, achievementRef)
    }

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_BEST_SCORE
}