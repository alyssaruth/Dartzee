package burlton.dartzee.code.achievements.x01

import burlton.core.code.util.Debug
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK
import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.ResourceCache.URL_ACHIEVEMENT_X01_SUCH_BAD_LUCK
import burlton.dartzee.code.utils.getAdjacentNumbers
import burlton.dartzee.code.utils.getCheckoutScores
import java.net.URL
import java.sql.SQLException

class AchievementX01SuchBadLuck: AbstractAchievement()
{
    override val name = "Such Bad Luck"
    override val desc = "Most adjacent doubles hit when on a checkout in a game of X01"
    override val achievementRef = ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK
    override val gameType = GAME_TYPE_X01

    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 3
    override val greenThreshold = 5
    override val blueThreshold = 7
    override val pinkThreshold = 10
    override val maxValue = 10

    override fun populateForConversion(playerIds: String)
    {
        val cols = "PlayerId VARCHAR(36), GameId VARCHAR(36), Score INT, Multiplier INT, StartingScore INT, DtLastUpdate TIMESTAMP"
        val tempTable = DatabaseUtil.createTempTable("CheckoutDarts", cols)

        tempTable ?: return

        val checkoutsStr = getCheckoutScores().joinToString()

        var sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT pt.PlayerId, pt.GameId, d.Score, d.Multiplier, d.StartingScore, d.DtLastUpdate")
        sb.append(" FROM Dart d, Participant pt, Game g")
        sb.append(" WHERE d.ParticipantId = pt.RowId")
        sb.append(" AND d.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $GAME_TYPE_X01")
        sb.append(" AND d.StartingScore IN ($checkoutsStr)")
        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        if (!DatabaseUtil.executeUpdate("" + sb))
        {
            DatabaseUtil.dropTable(tempTable)
            return
        }

        sb = StringBuilder()
        sb.append(" SELECT PlayerId, GameId, COUNT(1) AS GameTotal, MAX(DtLastUpdate) AS DtAchieved")
        sb.append(" FROM $tempTable")
        sb.append(" WHERE StartingScore = 50 AND Multiplier = 1 AND Score = 25")

        for (i in 1..20)
        {
            val adjacents = getAdjacentNumbers(i).joinToString()
            sb.append(" OR StartingScore = ${2*i} AND Multiplier = 2 AND Score IN ($adjacents)")
        }

        sb.append(" GROUP BY PlayerId, GameId")
        sb.append(" ORDER BY COUNT(1) DESC, DtAchieved")

        val playersAlreadyDone = mutableSetOf<String>()

        try
        {
            val rs = DatabaseUtil.executeQuery(sb)
            rs.use{
                while (rs.next())
                {
                    val playerId = rs.getString("PlayerId")
                    val gameId = rs.getString("GameId")
                    val total = rs.getInt("GameTotal")
                    val dtAchieved = rs.getTimestamp("DtAchieved")

                    if (playersAlreadyDone.contains(playerId))
                    {
                        continue
                    }

                    playersAlreadyDone.add(playerId)

                    AchievementEntity.factoryAndSave(ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK, playerId, gameId, total, "", dtAchieved)
                }
            }
        }
        catch (sqle: SQLException)
        {
            Debug.logSqlException("" + sb, sqle)
        }
        finally
        {
            DatabaseUtil.dropTable(tempTable)
        }
    }

    override fun getIconURL(): URL = URL_ACHIEVEMENT_X01_SUCH_BAD_LUCK

    override fun isUnbounded(): Boolean
    {
        return true
    }
}