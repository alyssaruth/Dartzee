package burlton.dartzee.code.achievements

class AchievementX01BestThreeDarts : AbstractAchievement()
{
    override val name = "Best Three Dart Score"

    override fun runConversion()
    {
        unlockThreeDartAchievement("drtLast.DtCreation", "drtLast.Ordinal = 3", ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE)
    }

}