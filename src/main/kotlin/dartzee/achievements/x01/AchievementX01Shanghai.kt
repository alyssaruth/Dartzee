package dartzee.achievements.x01

import dartzee.achievements.ACHIEVEMENT_REF_X01_SHANGHAI
import dartzee.achievements.AbstractAchievementRowPerGame
import dartzee.core.util.Debug
import dartzee.db.AchievementEntity
import dartzee.db.GAME_TYPE_X01
import dartzee.db.GameType
import dartzee.utils.DatabaseUtil
import dartzee.utils.ResourceCache.URL_ACHIEVEMENT_X01_SHANGHAI
import dartzee.utils.TOTAL_ROUND_SCORE_SQL_STR
import java.net.URL
import java.sql.SQLException

class AchievementX01Shanghai : AbstractAchievementRowPerGame()
{
    override val name = "Shanghai"
    override val desc = "Total number of times player has scored T20, D20, 20 (in any order)"
    override val achievementRef = ACHIEVEMENT_REF_X01_SHANGHAI
    override val gameType = GameType.X01

    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 3
    override val greenThreshold = 5
    override val blueThreshold = 7
    override val pinkThreshold = 10
    override val maxValue = 10

    override fun getIconURL(): URL = URL_ACHIEVEMENT_X01_SHANGHAI

    override fun getBreakdownColumns() = listOf("Game", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf(a.localGameIdEarned, a.dtLastUpdate)


    override fun populateForConversion(playerIds: String)
    {
        val tempTable = DatabaseUtil.createTempTable("Shanghai", "RoundNumber INT, ParticipantId VARCHAR(36), PlayerId VARCHAR(36), GameId VARCHAR(36)")

        var sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT DISTINCT drtFirst.RoundNumber, pt.RowId, pt.PlayerId, pt.GameId")
        sb.append(" FROM Dart drtFirst, Dart drtLast, Participant pt, Game g")
        sb.append(" WHERE drtFirst.ParticipantId = pt.RowId")
        sb.append(" AND drtFirst.PlayerId = pt.PlayerId")
        sb.append(" AND drtLast.ParticipantId = pt.RowId")
        sb.append(" AND drtLast.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $GAME_TYPE_X01")
        sb.append(" AND drtFirst.Ordinal = 1")
        sb.append(" AND drtLast.Ordinal = 3")
        sb.append(" AND drtLast.RoundNumber = drtFirst.RoundNumber")
        sb.append(" AND $TOTAL_ROUND_SCORE_SQL_STR = 120")
        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        if (!DatabaseUtil.executeUpdate("" + sb))
        {
            DatabaseUtil.dropTable(tempTable)
            return
        }

        //Cut down to where there is precisely 1 double, 1 treble and 1 single. Get the date achieved too.
        sb = StringBuilder()
        sb.append(" SELECT zz.PlayerId, zz.GameId, drtDouble.DtCreation AS DtAchieved")
        sb.append(" FROM $tempTable zz, Dart drtDouble, Dart drtTreble, Dart drtSingle")
        sb.append(" WHERE zz.ParticipantId = drtDouble.ParticipantId")
        sb.append(" AND zz.ParticipantId = drtTreble.ParticipantId")
        sb.append(" AND zz.ParticipantId = drtSingle.ParticipantId")
        sb.append(" AND zz.PlayerId = drtDouble.PlayerId")
        sb.append(" AND zz.PlayerId = drtTreble.PlayerId")
        sb.append(" AND zz.PlayerId = drtSingle.PlayerId")
        sb.append(" AND zz.RoundNumber = drtDouble.RoundNumber")
        sb.append(" AND zz.RoundNumber = drtTreble.RoundNumber")
        sb.append(" AND zz.RoundNumber = drtSingle.RoundNumber")
        sb.append(" AND drtDouble.Multiplier = 2")
        sb.append(" AND drtDouble.Score = 20")
        sb.append(" AND drtTreble.Multiplier = 3")
        sb.append(" AND drtTreble.Score = 20")
        sb.append(" AND drtSingle.Multiplier = 1")
        sb.append(" AND drtSingle.Score = 20")

        try
        {
            DatabaseUtil.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val playerId = rs.getString("PlayerId")
                    val gameId = rs.getString("GameId")
                    val dtAchieved = rs.getTimestamp("DtAchieved")

                    AchievementEntity.factoryAndSave(achievementRef, playerId, gameId, -1, "", dtAchieved)
                }
            }
        }
        catch (sqle: SQLException)
        {
            Debug.logSqlException(sb.toString(), sqle)
        }
        finally
        {
            DatabaseUtil.dropTable(tempTable)
        }
    }
}