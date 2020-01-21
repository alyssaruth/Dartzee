package dartzee.db.sanity

import dartzee.`object`.SEGMENT_TYPE_DOUBLE
import dartzee.`object`.SEGMENT_TYPE_INNER_SINGLE
import dartzee.`object`.SEGMENT_TYPE_OUTER_SINGLE
import dartzee.`object`.SEGMENT_TYPE_TREBLE
import dartzee.db.GAME_TYPE_GOLF
import dartzee.utils.DatabaseUtil

class SanityCheckFinalScoreGolf: AbstractSanityCheckFinalScore()
{
    override val gameType = GAME_TYPE_GOLF

    override fun populateParticipantToFinalScoreTable(tempTable: String)
    {
        val sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT pt.RowId, SUM(")
        sb.append(" 	CASE")
        sb.append(" 		WHEN drt.Score <> drt.RoundNumber THEN 5")
        sb.append(" 		WHEN drt.SegmentType = $SEGMENT_TYPE_DOUBLE THEN 1")
        sb.append(" 		WHEN drt.SegmentType = $SEGMENT_TYPE_TREBLE THEN 2")
        sb.append(" 		WHEN drt.SegmentType = $SEGMENT_TYPE_INNER_SINGLE THEN 3")
        sb.append(" 		WHEN drt.SegmentType = $SEGMENT_TYPE_OUTER_SINGLE THEN 4")
        sb.append(" 		ELSE 5")
        sb.append(" 	END")
        sb.append(" )")
        sb.append(" FROM Dart drt, Participant pt, Game g")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $GAME_TYPE_GOLF")
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
        DatabaseUtil.executeUpdate(sql)
    }
}