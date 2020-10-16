package dartzee.achievements.x01

import dartzee.achievements.ACHIEVEMENT_REF_X01_BTBF
import dartzee.achievements.AbstractAchievementRowPerGame
import dartzee.achievements.LAST_ROUND_FROM_PARTICIPANT
import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.ResourceCache.URL_ACHIEVEMENT_X01_BTBF
import java.net.URL

class AchievementX01Btbf: AbstractAchievementRowPerGame()
{
    override val achievementRef = ACHIEVEMENT_REF_X01_BTBF
    override val name = "BTBF"
    override val desc = "Number of games of X01 finished on D1"
    override val gameType = GameType.X01

    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 25
    override val blueThreshold = 50
    override val pinkThreshold = 100
    override val maxValue = pinkThreshold

    override fun getIconURL(): URL = URL_ACHIEVEMENT_X01_BTBF

    override fun getBreakdownColumns() = listOf("Game", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf(a.localGameIdEarned, a.dtLastUpdate)

    override fun populateForConversion(playerIds: String)
    {
        val sb = StringBuilder()

        sb.append(" SELECT pt.PlayerId, pt.DtFinished, g.RowId AS GameId")
        sb.append(" FROM Game g, Participant pt, Dart drt")
        sb.append(" WHERE g.GameType = '${GameType.X01}'")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND $LAST_ROUND_FROM_PARTICIPANT = drt.RoundNumber")
        sb.append(" AND pt.RowId = drt.ParticipantId")
        sb.append(" AND pt.PlayerId = drt.PlayerId")
        sb.append(" AND (drt.StartingScore - (drt.Score * drt.Multiplier)) = 0")
        sb.append(" AND drt.Score = 1")
        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        mainDatabase.executeQuery(sb).use { rs ->
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