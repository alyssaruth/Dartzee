package burlton.dartzee.code.achievements

import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.utils.DatabaseUtil

abstract class AbstractAchievementGamesWon : AbstractAchievement()
{
    override val redThreshold = 1
    override val orangeThreshold = 10
    override val yellowThreshold = 25
    override val greenThreshold = 50
    override val blueThreshold = 100
    override val pinkThreshold = 200
    override val maxValue = 200

    override fun populateForConversion(playerIds: String)
    {
        val sb = StringBuilder()
        sb.append(" SELECT PlayerId, COUNT(1) AS WinCount, MAX(pt.DtFinished) AS DtLastUpdate")
        sb.append(" FROM Participant pt, Game g")
        sb.append(" WHERE pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $gameType")
        sb.append(" AND pt.FinishingPosition = 1")
        if (!playerIds.isEmpty())
        {
            sb.append("AND PlayerId IN ($playerIds)")
        }
        sb.append(" GROUP BY PlayerId")

        DatabaseUtil.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getString("PlayerId")
                val score = rs.getInt("WinCount")
                val dtLastUpdate = rs.getTimestamp("DtLastUpdate")

                AchievementEntity.factoryAndSave(achievementRef, playerId, "", score, "", dtLastUpdate)
            }
        }
    }

    override fun isUnbounded(): Boolean
    {
        return true
    }
}
