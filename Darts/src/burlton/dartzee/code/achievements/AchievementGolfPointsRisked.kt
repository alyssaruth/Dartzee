package burlton.dartzee.code.achievements

import burlton.core.code.util.Debug
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GameEntity
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

    override fun populateForConversion(playerIds : String)
    {
        val sb = StringBuilder()

        val pointsRiskedSql = "5 - (CASE WHEN drtFirst.segmentType = 3 THEN 4 WHEN drtFirst.SegmentType = 4 THEN 3 ELSE drtFirst.SegmentType END)"

        sb.append(" SELECT pt.PlayerId, SUM($pointsRiskedSql) AS PointsRisked, MAX(drtFirst.DtCreation) AS DtLastUpdate")
        sb.append(" FROM Dart drtFirst, Round rnd, Participant pt, Game g")
        sb.append(" WHERE drtFirst.RoundId = rnd.RowId")
        sb.append(" AND rnd.ParticipantId = pt.RowId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = ${GameEntity.GAME_TYPE_GOLF}")
        sb.append(" AND rnd.RoundNumber = drtFirst.Score")
        sb.append(" AND drtFirst.Multiplier > 0")

        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        sb.append(" AND EXISTS (")
        sb.append("     SELECT 1")
        sb.append("     FROM Dart drt")
        sb.append("     WHERE drt.RoundId = drtFirst.RoundId")
        sb.append("     AND drt.Ordinal > drtFirst.Ordinal)")
        sb.append(" GROUP BY PlayerId")

        try
        {
            DatabaseUtil.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val playerId = rs.getLong("PlayerId")
                    val score = rs.getInt("PointsRisked")
                    val dtLastUpdate = rs.getTimestamp("DtLastUpdate")

                    AchievementEntity.factoryAndSave(achievementRef, playerId, -1, score, dtLastUpdate)
                }
            }
        }
        catch (sqle: SQLException)
        {
            Debug.logSqlException(sb.toString(), sqle)
        }
    }

    override fun getIconURL(): URL?
    {
        return ResourceCache.URL_ACHIEVEMENT_POINTS_RISKED
    }

    override fun isUnbounded(): Boolean
    {
        return true
    }
}