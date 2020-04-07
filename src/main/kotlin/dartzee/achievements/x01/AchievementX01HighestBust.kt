package dartzee.achievements.x01

import dartzee.achievements.ACHIEVEMENT_REF_X01_HIGHEST_BUST
import dartzee.achievements.AbstractAchievement
import dartzee.achievements.unlockThreeDartAchievement
import dartzee.db.GameType
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementX01HighestBust : AbstractAchievement()
{
    override val name = "Bognor"
    override val desc = "Highest number busted from in X01"
    override val achievementRef = ACHIEVEMENT_REF_X01_HIGHEST_BUST
    override val gameType = GameType.X01

    override val redThreshold = 2
    override val orangeThreshold = 20
    override val yellowThreshold = 40
    override val greenThreshold = 60
    override val blueThreshold = 80
    override val pinkThreshold = 100
    override val maxValue = 181

    override fun populateForConversion(playerIds : String)
    {
        val whereSql = "(drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) < 0  " +
                       "OR drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 1 " +
                       "OR ((drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 0) AND drtLast.Multiplier <> 2))"

        unlockThreeDartAchievement(playerIds, "drtLast.DtCreation", whereSql, "drtFirst.StartingScore", achievementRef)
    }

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_HIGHEST_BUST
}