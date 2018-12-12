package burlton.dartzee.code.achievements

import burlton.core.code.util.Debug
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.util.DateUtil
import java.sql.SQLException

abstract class AbstractAchievementGamesWon : AbstractAchievement()
{
    abstract val gameType : Int

    override val redThreshold = 1
    override val orangeThreshold = 10
    override val yellowThreshold = 25
    override val greenThreshold = 50
    override val blueThreshold = 100
    override val pinkThreshold = 200
    override val maxValue = pinkThreshold


    override fun populateForConversion(playerIds: String)
    {
        val sb = StringBuilder()
        sb.append(" SELECT PlayerId, COUNT(1) AS WinCount")
        sb.append(" FROM Participant pt, Game g")
        sb.append(" WHERE pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $gameType")
        sb.append(" AND pt.FinishingPosition = 1")
        if (!playerIds.isEmpty())
        {
            sb.append("AND PlayerId IN ($playerIds)")
        }
        sb.append(" GROUP BY PlayerId")


        try
        {
            DatabaseUtil.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val playerId = rs.getLong("PlayerId")
                    val score = rs.getInt("WinCount")

                    AchievementEntity.factoryAndSave(achievementRef, playerId, -1, score, DateUtil.getSqlDateNow())
                }
            }
        }
        catch (sqle: SQLException)
        {
            Debug.logSqlException(sb.toString(), sqle)
        }
    }

    override fun isUnbounded(): Boolean
    {
        return true
    }
}
