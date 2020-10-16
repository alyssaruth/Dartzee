package dartzee.achievements.x01

import dartzee.achievements.ACHIEVEMENT_REF_X01_NO_MERCY
import dartzee.achievements.AbstractAchievementRowPerGame
import dartzee.achievements.LAST_ROUND_FROM_PARTICIPANT
import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementX01NoMercy: AbstractAchievementRowPerGame()
{
    override val name = "No Mercy"
    override val desc = "Finishes from 3, 5, 7 or 9 in X01"
    override val achievementRef = ACHIEVEMENT_REF_X01_NO_MERCY
    override val gameType = GameType.X01

    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 3
    override val greenThreshold = 5
    override val blueThreshold = 7
    override val pinkThreshold = 10
    override val maxValue = 10

    override fun populateForConversion(playerIds: String)
    {
        val sb = StringBuilder()
        sb.append(" SELECT drt.StartingScore, pt.PlayerId, pt.GameId, pt.DtFinished")
        sb.append(" FROM Game g, Participant pt, Dart drt")
        sb.append(" WHERE pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.X01}'")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" AND $LAST_ROUND_FROM_PARTICIPANT = drt.RoundNumber")
        sb.append(" AND pt.RowId = drt.ParticipantId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND drt.Ordinal = 1")
        sb.append(" AND drt.StartingScore IN (3, 5, 7, 9)")
        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        mainDatabase.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getString("PlayerId")
                val score = rs.getInt("StartingScore")
                val gameId = rs.getString("GameId")
                val dtAchieved = rs.getTimestamp("DtFinished")

                AchievementEntity.factoryAndSave(ACHIEVEMENT_REF_X01_NO_MERCY, playerId, gameId, -1, "$score", dtAchieved)
            }
        }
    }

    override fun getBreakdownColumns() = listOf("Checkout", "Game", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf(a.achievementDetail, a.localGameIdEarned, a.dtLastUpdate)

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_X01_NO_MERCY
}