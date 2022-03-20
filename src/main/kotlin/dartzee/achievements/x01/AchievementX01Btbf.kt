package dartzee.achievements.x01

import dartzee.achievements.*
import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache.URL_ACHIEVEMENT_X01_BTBF
import java.net.URL

class AchievementX01Btbf: AbstractMultiRowAchievement()
{
    override val achievementType = AchievementType.X01_BTBF
    override val name = "BTBF"
    override val desc = "Number of games of X01 finished on D1"
    override val gameType = GameType.X01

    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 25
    override val blueThreshold = 50
    override val pinkThreshold = 100
    override val maxValue = pinkThreshold

    override fun getIconURL() = URL_ACHIEVEMENT_X01_BTBF

    override fun getBreakdownColumns() = listOf("Game", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf<Any>(a.localGameIdEarned, a.dtAchieved)

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        ensureX01RoundsTableExists(playerIds, database)

        val sb = StringBuilder()
        sb.append(" SELECT PlayerId, DtRoundFinished AS DtAchieved, GameId")
        sb.append(" FROM $X01_ROUNDS_TABLE")
        sb.append(" WHERE LastDartScore = 1")
        sb.append(" AND LastDartMultiplier = 2")
        sb.append(" AND RemainingScore = 0")

        database.executeQuery(sb).use { bulkInsertFromResultSet(it, database, achievementType) }
    }
}