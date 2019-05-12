package burlton.dartzee.code.achievements.golf

import burlton.core.code.util.Debug
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_GOLF_POINTS_RISKED
import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.ResourceCache
import java.net.URL
import java.sql.SQLException

class AchievementGolfPointsRisked : AbstractAchievement()
{
    override val name = "Gambler"
    override val desc = "Total number of points risked (by continuing to throw) in Golf"
    override val achievementRef = ACHIEVEMENT_REF_GOLF_POINTS_RISKED
    override val redThreshold = 5
    override val orangeThreshold = 10
    override val yellowThreshold = 25
    override val greenThreshold = 50
    override val blueThreshold = 100
    override val pinkThreshold = 200
    override val maxValue = 200

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_POINTS_RISKED
    override fun isUnbounded() = true

    override fun populateForConversion(playerIds : String)
    {
        val sb = StringBuilder()

        val pointsRiskedSql = "5 - (CASE WHEN drtFirst.segmentType = 3 THEN 4 WHEN drtFirst.SegmentType = 4 THEN 3 ELSE drtFirst.SegmentType END)"

        sb.append(" SELECT pt.PlayerId, SUM($pointsRiskedSql) AS PointsRisked, MAX(drtFirst.DtCreation) AS DtLastUpdate")
        sb.append(" FROM Dart drtFirst, Participant pt, Game g")
        sb.append(" WHERE drtFirst.ParticipantId = pt.RowId")
        sb.append(" AND drtFirst.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $GAME_TYPE_GOLF")
        sb.append(" AND drtFirst.RoundNumber = drtFirst.Score")
        sb.append(" AND drtFirst.Multiplier > 0")

        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        sb.append(" AND EXISTS (")
        sb.append("     SELECT 1")
        sb.append("     FROM Dart drt")
        sb.append("     WHERE drt.ParticipantId = drtFirst.ParticipantId")
        sb.append("     AND drt.PlayerId = drtFirst.PlayerId")
        sb.append("     AND drt.RoundNumber = drtFirst.RoundNumber")
        sb.append("     AND drt.Ordinal > drtFirst.Ordinal)")
        sb.append(" GROUP BY pt.PlayerId")

        try
        {
            DatabaseUtil.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val playerId = rs.getString("PlayerId")
                    val score = rs.getInt("PointsRisked")
                    val dtLastUpdate = rs.getTimestamp("DtLastUpdate")

                    AchievementEntity.factoryAndSave(achievementRef, playerId, "", score, "", dtLastUpdate)
                }
            }
        }
        catch (sqle: SQLException)
        {
            Debug.logSqlException(sb.toString(), sqle)
        }
    }
}