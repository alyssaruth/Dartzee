package dartzee.achievements.x01

import dartzee.achievements.*
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache
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

    override fun populateForConversion(players: List<PlayerEntity>, database: Database)
    {
        val dartSql = "TotalDartsThrown = 3 AND (RemainingScore > 0 OR (RemainingScore = 0 AND LastDartMultiplier = 2))"
        unlockThreeDartAchievement(players, dartSql, "StartingScore - RemainingScore", achievementRef, database)
    }

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_BEST_SCORE
}