package dartzee.achievements.x01

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.unlockThreeDartAchievement
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache

class AchievementX01BestThreeDarts : AbstractAchievement() {
    override val name = "Three Darter"
    override val desc = "Best three dart score in X01"
    override val achievementType = AchievementType.X01_BEST_THREE_DART_SCORE
    override val gameType = GameType.X01

    override val redThreshold = 60
    override val orangeThreshold = 80
    override val yellowThreshold = 100
    override val greenThreshold = 121
    override val blueThreshold = 141
    override val pinkThreshold = 171
    override val maxValue = 180
    override val allowedForTeams = true

    override fun populateForConversion(playerIds: List<String>, database: Database) {
        val dartSql =
            "TotalDartsThrown = 3 AND (RemainingScore > 0 OR (RemainingScore = 0 AND LastDartMultiplier = 2))"
        unlockThreeDartAchievement(
            playerIds,
            dartSql,
            "StartingScore - RemainingScore",
            achievementType,
            database
        )
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_BEST_SCORE
}
