package dartzee.achievements.rtc

import dartzee.achievements.ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES
import dartzee.achievements.AbstractAchievement
import dartzee.db.AchievementEntity
import dartzee.db.CLOCK_TYPE_DOUBLES
import dartzee.db.CLOCK_TYPE_STANDARD
import dartzee.db.CLOCK_TYPE_TREBLES
import dartzee.game.GameType
import dartzee.utils.DatabaseUtil
import dartzee.utils.InjectedThings.logger
import dartzee.utils.ResourceCache
import java.net.URL
import java.sql.SQLException

class AchievementClockBruceyBonuses : AbstractAchievement()
{
    override val name = "Didn't he do well!?"
    override val desc = "Total number of 'Brucey Bonuses' executed in Round the Clock"
    override val achievementRef = ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES
    override val gameType = GameType.ROUND_THE_CLOCK

    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 25
    override val blueThreshold = 50
    override val pinkThreshold = 100
    override val maxValue = 100

    override fun isUnbounded() = true

    override fun populateForConversion(playerIds: String)
    {
        val sb = StringBuilder()
        sb.append(" SELECT pt.PlayerId, COUNT(1) AS BruceCount, MAX(drt.DtCreation) AS DtLastUpdate")
        sb.append(" FROM Dart drt, Participant pt, Game g")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.ROUND_THE_CLOCK}'")
        sb.append(" AND drt.Ordinal = 4")
        sb.append(" AND drt.Score = drt.StartingScore")
        sb.append(" AND (")
        sb.append("        (g.GameParams = '$CLOCK_TYPE_STANDARD' AND drt.Multiplier > 0)")
        sb.append("     OR (g.GameParams = '$CLOCK_TYPE_DOUBLES' AND drt.Multiplier = 2)")
        sb.append("     OR (g.GameParams = '$CLOCK_TYPE_TREBLES' AND drt.Multiplier = 3)")
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
                    val playerId = rs.getString("PlayerId")
                    val score = rs.getInt("BruceCount")
                    val dtLastUpdate = rs.getTimestamp("DtLastUpdate")

                    AchievementEntity.factoryAndSave(achievementRef, playerId, "", score, "", dtLastUpdate)
                }
            }
        }
        catch (sqle: SQLException)
        {
            logger.logSqlException(sb.toString(), sb.toString(), sqle)
        }
    }

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_CLOCK_BRUCEY_BONUSES
}