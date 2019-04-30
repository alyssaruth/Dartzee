package burlton.dartzee.code.achievements

import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.ResourceCache.URL_ACHIEVEMENT_X01_BTBF
import java.net.URL

class AchievementX01Btbf: AbstractAchievementRowPerGame()
{
    override val achievementRef = ACHIEVEMENT_REF_X01_BTBF
    override val name = "BTBF"
    override val desc = "Number of games of X01 finished on D1"

    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 25
    override val blueThreshold = 50
    override val pinkThreshold = 100
    override val maxValue = pinkThreshold

    override fun getIconURL(): URL = URL_ACHIEVEMENT_X01_BTBF

    override fun populateForConversion(playerIds: String)
    {
        val sb = StringBuilder()

        sb.append(" SELECT pt.PlayerId, pt.DtFinished, g.RowId AS GameId")
        sb.append(" FROM Game g, Participant pt, Round rnd, Dart drt")
        sb.append(" WHERE g.GameType = $GAME_TYPE_X01")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND CEIL(CAST(pt.FinalScore AS DECIMAL)/3) = rnd.RoundNumber")
        sb.append(" AND pt.RowId = rnd.ParticipantId")
        sb.append(" AND drt.RoundId = rnd.RowId")
        sb.append(" AND (drt.StartingScore - (drt.Score * drt.Multiplier)) = 0")
        sb.append(" AND drt.Score = 1")
        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        DatabaseUtil.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getString("PlayerId")
                val gameId = rs.getString("GameId")
                val dtAchieved = rs.getTimestamp("DtFinished")

                AchievementEntity.factoryAndSave(achievementRef, playerId, gameId, -1, "", dtAchieved)
            }
        }
    }
}