package dartzee.achievements.x01

import dartzee.achievements.AchievementType
import dartzee.utils.ResourceCache.URL_ACHIEVEMENT_X01_HOTEL_INSPECTOR

class AchievementX01HotelInspector : AbstractAchievementX01ScoreVariants(true) {
    override val targetScore = 26
    override val name = "Hotel Inspector"
    override val desc = "Number of 3-dart ways the player has scored 26 (\"Bed and Breakfast\")"
    override val achievementType = AchievementType.X01_HOTEL_INSPECTOR

    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 15
    override val blueThreshold = 20
    override val pinkThreshold = 26
    override val maxValue = 26

    override fun getIconURL() = URL_ACHIEVEMENT_X01_HOTEL_INSPECTOR
}
