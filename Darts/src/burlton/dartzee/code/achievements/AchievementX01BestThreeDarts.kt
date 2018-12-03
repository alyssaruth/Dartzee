package burlton.dartzee.code.achievements

class AchievementX01BestThreeDarts : AbstractAchievement()
{
    override val name = "Best Three Dart Score"
    override val achievementRef = ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE

    override val redThreshold = 60
    override val orangeThreshold = 80
    override val yellowThreshold = 100
    override val greenThreshold = 121
    override val blueThreshold = 141
    override val pinkThreshold = 171
    override val maxValue = 180

    override fun populateForConversion(playerIds: String)
    {
        unlockThreeDartAchievement(playerIds, "drtLast.DtCreation", "drtLast.Ordinal = 3", achievementRef)
    }

}