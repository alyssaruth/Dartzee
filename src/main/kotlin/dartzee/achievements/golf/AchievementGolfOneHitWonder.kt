package dartzee.achievements.golf

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache

class AchievementGolfOneHitWonder : AbstractAchievement()
{
    override val name = "One hit wonder"
    override val desc = "Most holes-in-one in a single game of Golf"
    override val achievementType = AchievementType.GOLF_ONE_HIT_WONDER
    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 3
    override val greenThreshold = 4
    override val blueThreshold = 6
    override val pinkThreshold = 9
    override val maxValue = 18
    override val gameType = GameType.GOLF
    override val allowedForTeams = false

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        TODO("Not yet implemented")
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_GOLF_ONE_HIT_WONDER
}