package burlton.dartzee.code.achievements

class AchievementX01BestThreeDarts : AbstractAchievement()
{
    override val name = "Best Three Dart Score"
    override val achievementRef = ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE

    override fun populateForConversion(playerIds: String)
    {
        unlockThreeDartAchievement(playerIds, "drtLast.DtCreation", "drtLast.Ordinal = 3", achievementRef)
    }

}