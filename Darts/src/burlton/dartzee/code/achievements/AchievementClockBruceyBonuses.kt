package burlton.dartzee.code.achievements

import burlton.core.code.util.Debug
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.ResourceCache
import burlton.desktopcore.code.util.DateUtil
import java.net.URL
import java.sql.SQLException

class AchievementClockBruceyBonuses : AbstractAchievement()
{
    override val name = "Didn't he do well!?"
    override val achievementRef = ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES

    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 25
    override val blueThreshold = 50
    override val pinkThreshold = 100
    override val maxValue = 100

    override fun isUnbounded(): Boolean
    {
        return true
    }

    override fun populateForConversion(playerIds: String)
    {
        val sb = StringBuilder()
        sb.append(" SELECT pt.PlayerId, COUNT(1) AS BruceCount")
        sb.append(" FROM Dart drt, Round rnd, Participant pt, Game g")
        sb.append(" WHERE drt.RoundId = rnd.RowId")
        sb.append(" AND rnd.ParticipantId = pt.RowId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = ${GameEntity.GAME_TYPE_ROUND_THE_CLOCK}")
        sb.append(" AND drt.Ordinal = 4")
        sb.append(" AND drt.Score = drt.StartingScore")
        sb.append(" AND (")
        sb.append("        (g.GameParams = '${GameEntity.CLOCK_TYPE_STANDARD}' AND drt.Multiplier > 0)")
        sb.append("     OR (g.GameParams = '${GameEntity.CLOCK_TYPE_DOUBLES}' AND drt.Multiplier = 2)")
        sb.append("     OR (g.GameParams = '${GameEntity.CLOCK_TYPE_TREBLES}' AND drt.Multiplier = 3)")
        sb.append(" )")
        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN($playerIds)")
        }
        sb.append(" GROUP BY pt.PlayerId")

        try
        {
            DatabaseUtil.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val playerId = rs.getLong("PlayerId")
                    val score = rs.getInt("BruceCount")

                    AchievementEntity.factoryAndSave(achievementRef, playerId, -1, score, DateUtil.getSqlDateNow())
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
        return ResourceCache.URL_ACHIEVEMENT_CLOCK_BRUCEY_BONUSES
    }
}