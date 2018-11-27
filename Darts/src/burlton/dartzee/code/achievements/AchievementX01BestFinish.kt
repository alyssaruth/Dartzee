package burlton.dartzee.code.achievements

class AchievementX01BestFinish : AbstractAchievement()
{
    override val name = "Best Finish"

    override fun runConversion()
    {
        val whereSql = "drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 0  " +
                       "AND drtLast.Multiplier = 2"

        unlockThreeDartAchievement("pt.DtFinished", whereSql, ACHIEVEMENT_REF_X01_BEST_FINISH)
    }
}