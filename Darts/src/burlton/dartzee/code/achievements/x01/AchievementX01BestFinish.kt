package burlton.dartzee.code.achievements.x01

import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_BEST_FINISH
import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.achievements.unlockThreeDartAchievement
import burlton.dartzee.code.screen.stats.overall.TOTAL_ROUND_SCORE_SQL_STR
import burlton.dartzee.code.utils.ResourceCache
import burlton.desktopcore.code.util.getEndOfTimeSqlString
import java.net.URL

class AchievementX01BestFinish : AbstractAchievement()
{
    override val name = "Finisher"
    override val desc = "Highest checkout in X01"
    override val achievementRef = ACHIEVEMENT_REF_X01_BEST_FINISH
    override val redThreshold = 2
    override val orangeThreshold = 41
    override val yellowThreshold = 61
    override val greenThreshold = 81
    override val blueThreshold = 121
    override val pinkThreshold = 170
    override val maxValue = 170

    override fun populateForConversion(playerIds : String)
    {
        val whereSql = "drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 0  " +
                       "AND drtLast.Multiplier = 2 " +
                       "AND pt.DtFinished < ${getEndOfTimeSqlString()}"

        unlockThreeDartAchievement(playerIds, "pt.DtFinished", whereSql, TOTAL_ROUND_SCORE_SQL_STR, achievementRef)
    }

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_BEST_FINISH
}