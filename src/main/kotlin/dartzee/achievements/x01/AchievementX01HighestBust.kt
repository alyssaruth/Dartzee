package dartzee.achievements.x01

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.unlockThreeDartAchievement
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache

class AchievementX01HighestBust : AbstractAchievement()
{
    override val name = "Bognor"
    override val desc = "Highest number busted from in X01"
    override val achievementType = AchievementType.X01_HIGHEST_BUST
    override val gameType = GameType.X01
    override val allowedForTeams = true

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

        unlockThreeDartAchievement(playerIds, whereSql, "StartingScore", achievementType, database)
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_HIGHEST_BUST
}