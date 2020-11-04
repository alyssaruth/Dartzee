package dartzee.achievements.x01

import dartzee.achievements.ACHIEVEMENT_REF_X01_HIGHEST_BUST
import dartzee.achievements.AbstractAchievement
import dartzee.achievements.unlockThreeDartAchievement
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementX01HighestBust : AbstractAchievement()
{
    override val name = "Bognor"
    override val desc = "Highest number busted from in X01"
    override val achievementRef = ACHIEVEMENT_REF_X01_HIGHEST_BUST
    override val gameType = GameType.X01

    override val redThreshold = 2
    override val orangeThreshold = 20
    override val yellowThreshold = 40
    override val greenThreshold = 60
    override val blueThreshold = 80
    override val pinkThreshold = 100
    override val maxValue = 181

    override fun populateForConversion(players: List<PlayerEntity>)
    {
        val whereSql = "(drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) < 0  " +
                       "OR drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 1 " +
                       "OR ((drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 0) AND drtLast.Multiplier <> 2))"

        unlockThreeDartAchievement(players, whereSql, "rnd.StartingScore", achievementRef, mainDatabase)
    }

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_HIGHEST_BUST
}