package burlton.dartzee.code.achievements

class AchievementX01HighestBust : AbstractAchievement()
{
    override val name = "Highest bust"
    override val achievementRef = ACHIEVEMENT_REF_X01_HIGHEST_BUST
    override val redThreshold = 2
    override val orangeThreshold = 20
    override val yellowThreshold = 40
    override val greenThreshold = 60
    override val blueThreshold = 80
    override val pinkThreshold = 100
    override val maxValue = 180

    override fun populateForConversion(playerIds : String)
    {
        val whereSql = "(drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) < 0  " +
                       "OR drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 1 " +
                       "OR ((drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 0) AND drtLast.Multiplier <> 2))"

        unlockThreeDartAchievement(playerIds, "drtLast.DtCreation", whereSql, "drtFirst.StartingScore", achievementRef)
    }
}