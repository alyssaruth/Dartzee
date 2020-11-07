package dartzee.achievements.x01

import dartzee.achievements.ACHIEVEMENT_REF_X01_HIGHEST_BUST
import dartzee.achievements.AbstractAchievement
import dartzee.achievements.unlockThreeDartAchievement
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.utils.Database
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

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        val whereSql = "RemainingScore < 0 OR RemainingSCore = 1 OR (RemainingScore = 0 AND LastDartMultiplier <> 2)"

        unlockThreeDartAchievement(playerIds, whereSql, "StartingScore", achievementRef, database)
    }

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_HIGHEST_BUST
}