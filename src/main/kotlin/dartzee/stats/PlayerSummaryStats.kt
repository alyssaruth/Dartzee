package dartzee.stats

import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.utils.DatabaseUtil

data class ParticipantSummary(val gameType: GameType, val finalScore: Int, val finishingPosition: Int)

fun getParticipantSummaries(player: PlayerEntity): List<ParticipantSummary>
{
    val list = mutableListOf<ParticipantSummary>()

    val query = "SELECT g.GameType, pt.FinishingPosition, pt.FinalScore FROM Participant pt, Game g WHERE pt.GameId = g.RowId AND pt.PlayerId = '${player.rowId}'"
    DatabaseUtil.executeQuery(query).use { rs ->
        while (rs.next())
        {
            val gameType = GameType.valueOf(rs.getString("GameType"))
            val finishingPosition = rs.getInt("FinishingPosition")
            val finalScore = rs.getInt("FinalScore")

            list.add(ParticipantSummary(gameType, finalScore, finishingPosition))
        }
    }

    return list.toList()
}