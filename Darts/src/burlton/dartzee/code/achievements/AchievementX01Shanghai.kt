package burlton.dartzee.code.achievements

import burlton.core.code.util.Debug
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.stats.overall.TOTAL_ROUND_SCORE_SQL_STR
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.ResourceCache.URL_ACHIEVEMENT_X01_SHANGHAI
import java.net.URL
import java.sql.SQLException

class AchievementX01Shanghai : AbstractAchievement()
{
    override val name = "Shanghai"
    override val desc = "Total number of times player has scored T20, D20, 20 (in any order)"
    override val achievementRef = ACHIEVEMENT_REF_X01_SHANGHAI
    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 3
    override val greenThreshold = 5
    override val blueThreshold = 7
    override val pinkThreshold = 10
    override val maxValue = 10

    override fun getIconURL(): URL?
    {
        return URL_ACHIEVEMENT_X01_SHANGHAI
    }

    override fun isUnbounded(): Boolean
    {
        return true
    }

    override fun initialiseFromDb(achievementRows: MutableList<AchievementEntity>, player: PlayerEntity?)
    {
        this.player = player

        attainedValue = achievementRows.size

        if (!achievementRows.isEmpty())
        {
            val last = achievementRows.sortedBy { it.dtLastUpdate }.last()

            dtLatestUpdate = last.dtLastUpdate
            gameIdEarned = last.gameIdEarned
        }
    }

    override fun populateForConversion(playerIds: String)
    {
        val tempTable = DatabaseUtil.createTempTable("Shanghai", "RoundId INT, ParticipantId INT, PlayerId INT, GameId INT")

        var sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT DISTINCT drtFirst.RoundId, rnd.ParticipantId, pt.PlayerId, pt.GameId")
        sb.append(" FROM Dart drtFirst, Dart drtLast, Round rnd, Participant pt, Game g")
        sb.append(" WHERE drtFirst.RoundId = rnd.RowId")
        sb.append(" AND rnd.ParticipantId = pt.RowId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = ${GameEntity.GAME_TYPE_X01}")
        sb.append(" AND drtFirst.Ordinal = 1")
        sb.append(" AND drtLast.Ordinal = 3")
        sb.append(" AND drtLast.RoundId = drtFirst.RoundId")
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
        sb.append(" WHERE zz.RoundId = drtDouble.RoundId")
        sb.append(" AND zz.RoundId = drtTreble.RoundId")
        sb.append(" AND zz.RoundId = drtSingle.RoundId")
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
                    val playerId = rs.getLong("PlayerId")
                    val gameId = rs.getLong("GameId")
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