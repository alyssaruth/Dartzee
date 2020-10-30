package dartzee.achievements.golf

import dartzee.achievements.ACHIEVEMENT_REF_GOLF_POINTS_RISKED
import dartzee.achievements.AbstractMultiRowAchievement
import dartzee.achievements.getGolfSegmentCases
import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementGolfPointsRisked : AbstractMultiRowAchievement()
{
    override val name = "Gambler"
    override val desc = "Total number of points risked (by continuing to throw) in Golf"
    override val gameType = GameType.GOLF

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

    override fun getBreakdownColumns() = listOf("Game", "Round", "Points risked", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf(a.localGameIdEarned, a.achievementDetail.toInt(), a.achievementCounter, a.dtLastUpdate)
    override fun useCounter() = true

    private fun buildPointsRiskedSql(): String
    {
        val sb = StringBuilder()
        sb.append("5 - CASE")
        sb.append(getGolfSegmentCases())
        sb.append(" END")

        return sb.toString()
    }

    override fun populateForConversion(playerIds: String, database: Database)
    {
        val sb = StringBuilder()

        sb.append(" SELECT pt.PlayerId, pt.GameId, drt.RoundNumber, SUM(${buildPointsRiskedSql()}) AS PointsRisked, MAX(drt.DtCreation) AS DtLastUpdate")
        sb.append(" FROM Dart drt, Participant pt, Game g")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.GOLF}'")
        sb.append(" AND drt.RoundNumber = drt.Score")
        sb.append(" AND drt.Multiplier > 0")
        if (playerIds.isNotEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        sb.append(" AND EXISTS (")
        sb.append("     SELECT 1")
        sb.append("     FROM Dart drtOther")
        sb.append("     WHERE drtOther.ParticipantId = drt.ParticipantId")
        sb.append("     AND drtOther.PlayerId = drt.PlayerId")
        sb.append("     AND drtOther.RoundNumber = drt.RoundNumber")
        sb.append("     AND drtOther.Ordinal > drt.Ordinal)")
        sb.append(" GROUP BY pt.PlayerId, pt.GameId, drt.RoundNumber")

        database.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getString("PlayerId")
                val gameId = rs.getString("GameId")
                val roundNumber = rs.getInt("RoundNumber")
                val score = rs.getInt("PointsRisked")
                val dtLastUpdate = rs.getTimestamp("DtLastUpdate")

                AchievementEntity.factoryAndSave(achievementRef, playerId, gameId, score, "$roundNumber", dtLastUpdate, database)
            }
        }
    }
}