package dartzee.achievements.x01

import dartzee.achievements.ACHIEVEMENT_REF_X01_BEST_FINISH
import dartzee.achievements.AbstractAchievement
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementX01BestFinish : AbstractAchievement()
{
    override val name = "Finisher"
    override val desc = "Highest checkout in X01"
    override val achievementRef = ACHIEVEMENT_REF_X01_BEST_FINISH
    override val gameType = GameType.X01

    override val redThreshold = 2
    override val orangeThreshold = 41
    override val yellowThreshold = 61
    override val greenThreshold = 81
    override val blueThreshold = 121
    override val pinkThreshold = 170
    override val maxValue = 170

    override fun populateForConversion(players: List<PlayerEntity>, database: Database)
    {
        val sb = StringBuilder()
        sb.append(" SELECT finish.GameId, finish.PlayerId, finish.Finish, finish.DtCreation AS DtAchieved")
        sb.append(" FROM X01Finish finish")
        sb.append(" ORDER BY DtCreation")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(rs, database, achievementRef, oneRowPerPlayer = true, achievementCounterFn = { rs.getInt("Finish") })
        }
    }

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_BEST_FINISH
}