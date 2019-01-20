package burlton.dartzee.code.achievements

import burlton.core.code.util.Debug
import burlton.dartzee.code.db.*
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.ResourceCache
import java.net.URL
import java.sql.SQLException

class AchievementClockBruceyBonuses : AbstractAchievement()
{
    override val name = "Didn't he do well!?"
    override val desc = "Total number of 'Brucey Bonuses' executed in Round the Clock"
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
        val sb = StringBuilder().apply {
            append(" SELECT pt.PlayerId, COUNT(1) AS BruceCount, MAX(drt.DtCreation) AS DtLastUpdate")
            append(" FROM Dart drt, Round rnd, Participant pt, Game g")
            append(" WHERE drt.RoundId = rnd.RowId")
            append(" AND rnd.ParticipantId = pt.RowId")
            append(" AND pt.GameId = g.RowId")
            append(" AND g.GameType = $GAME_TYPE_ROUND_THE_CLOCK")
            append(" AND drt.Ordinal = 4")
            append(" AND drt.Score = drt.StartingScore")
            append(" AND (")
            append("        (g.GameParams = '$CLOCK_TYPE_STANDARD' AND drt.Multiplier > 0)")
            append("     OR (g.GameParams = '$CLOCK_TYPE_DOUBLES' AND drt.Multiplier = 2)")
            append("     OR (g.GameParams = '$CLOCK_TYPE_TREBLES' AND drt.Multiplier = 3)")
            append(" )")
        }
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
                    val dtLastUpdate = rs.getTimestamp("DtLastUpdate")

                    AchievementEntity.factoryAndSave(achievementRef, playerId, -1, score, "", dtLastUpdate)
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