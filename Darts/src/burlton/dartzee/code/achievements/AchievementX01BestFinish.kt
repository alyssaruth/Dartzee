package burlton.dartzee.code.achievements

class AchievementX01BestFinish : AbstractAchievement()
{
    override val name = "Best Finish"
    override val achievementRef = ACHIEVEMENT_REF_X01_BEST_FINISH
    override val redThreshold = 2
    override val orangeThreshold = 41
    override val yellowThreshold = 61
    override val greenThreshold = 81
    override val blueThreshold = 121
    override val pinkThreshold = 170
    override val maxValue = 170

    override fun populateForConversion(playerIds : String)
    {
        val whereSql = "drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 0  " +
                       "AND drtLast.Multiplier = 2"

        unlockThreeDartAchievement(playerIds, "pt.DtFinished", whereSql, achievementRef)
    }
}