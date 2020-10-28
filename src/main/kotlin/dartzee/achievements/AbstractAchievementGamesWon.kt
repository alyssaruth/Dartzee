package dartzee.achievements

import dartzee.db.AchievementEntity
import dartzee.utils.Database

abstract class AbstractAchievementGamesWon : AbstractMultiRowAchievement()
{
    override val redThreshold = 1
    override val orangeThreshold = 10
    override val yellowThreshold = 25
    override val greenThreshold = 50
    override val blueThreshold = 100
    override val pinkThreshold = 200
    override val maxValue = 200

    override fun populateForConversion(playerIds: String, database: Database)
    {
        val sb = StringBuilder()
        sb.append(" SELECT pt.PlayerId, pt.GameId, pt.FinalScore, pt.DtFinished AS DtLastUpdate")
        sb.append(" FROM Participant pt, Game g")
        sb.append(" WHERE pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '$gameType'")
        sb.append(" AND pt.FinishingPosition = 1")
        if (playerIds.isNotEmpty())
        {
            sb.append("AND PlayerId IN ($playerIds)")
        }

        database.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getString("PlayerId")
                val gameId = rs.getString("GameId")
                val finalScore = rs.getInt("FinalScore")
                val dtLastUpdate = rs.getTimestamp("DtLastUpdate")

                AchievementEntity.factoryAndSave(achievementRef, playerId, gameId, -1, "$finalScore", dtLastUpdate, database)
            }
        }
    }

    override fun getBreakdownColumns() = listOf("Game", "Score", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf(a.localGameIdEarned, a.achievementDetail.toInt(), a.dtLastUpdate)
}
