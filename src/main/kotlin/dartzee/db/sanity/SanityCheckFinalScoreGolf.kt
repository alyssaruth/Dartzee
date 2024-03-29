package dartzee.db.sanity

import dartzee.achievements.getGolfSegmentCases
import dartzee.game.GameType
import dartzee.utils.InjectedThings.mainDatabase

class SanityCheckFinalScoreGolf : AbstractSanityCheckFinalScore() {
    override val gameType = GameType.GOLF

    override fun populateParticipantToFinalScoreTable(tempTable: String) {
        val sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT pt.RowId, SUM(")
        sb.append(" 	CASE")
        sb.append(" 		WHEN drt.Score <> drt.RoundNumber THEN 5")
        sb.append(getGolfSegmentCases())
        sb.append(" 	END")
        sb.append(" )")
        sb.append(" FROM Dart drt, Participant pt, Game g")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.GOLF}'")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" AND NOT EXISTS")
        sb.append(" (")
        sb.append("     SELECT 1")
        sb.append("     FROM Dart drt2")
        sb.append("     WHERE drt.ParticipantId = drt2.ParticipantId")
        sb.append("     AND drt.PlayerId = drt2.PlayerId")
        sb.append("     AND drt.RoundNumber = drt2.RoundNumber")
        sb.append("     AND drt2.Ordinal > drt.Ordinal")
        sb.append(" )")
        sb.append(" GROUP BY pt.RowID, pt.FinalScore")

        val sql = sb.toString()
        mainDatabase.executeUpdate(sql)
    }
}
