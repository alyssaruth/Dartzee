package dartzee.db.sanity

import dartzee.game.GameType
import dartzee.utils.DatabaseUtil

class SanityCheckFinalScoreRtc: AbstractSanityCheckFinalScore()
{
    override val gameType = GameType.ROUND_THE_CLOCK

    override fun populateParticipantToFinalScoreTable(tempTable: String)
    {
        val sb = StringBuilder()
        sb.append("INSERT INTO $tempTable")
        sb.append(" SELECT pt.RowId, COUNT(1)")
        sb.append(" FROM Game g, Participant pt, Dart drt")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.ROUND_THE_CLOCK}'")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" GROUP BY pt.RowId")

        val sql = sb.toString()
        DatabaseUtil.executeUpdate(sql)
    }
}
