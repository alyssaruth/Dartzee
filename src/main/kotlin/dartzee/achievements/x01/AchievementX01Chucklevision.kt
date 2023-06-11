package dartzee.achievements.x01

import dartzee.achievements.AchievementType
import dartzee.utils.ResourceCache

class AchievementX01Chucklevision : AbstractAchievementX01ScoreVariants()
{
    override val targetScore = 69
    override val name = "Chucklevision"
    override val desc = "Number of distinct ways the player has scored 69 (\"Chucklevision\")"
    override val achievementType = AchievementType.X01_CHUCKLEVISION

    override val redThreshold = 1
    override val orangeThreshold = 10
    override val yellowThreshold = 20
    override val greenThreshold = 35
    override val blueThreshold = 50
    override val pinkThreshold = 69
    override val maxValue = 69

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_X01_CHUCKLEVISION
}