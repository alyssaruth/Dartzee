package burlton.dartzee.code.achievements

class AchievementX01BestFinish : AbstractAchievement()
{
    override val name = "Best Finish"
    override val achievementRef = ACHIEVEMENT_REF_X01_BEST_FINISH

    override fun populateForConversion(playerIds : String)
    {
        val whereSql = "drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 0  " +
                       "AND drtLast.Multiplier = 2"

        unlockThreeDartAchievement(playerIds, "pt.DtFinished", whereSql, achievementRef)
    }
}