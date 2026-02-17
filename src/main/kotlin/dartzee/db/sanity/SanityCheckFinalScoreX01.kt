package dartzee.db.sanity

import dartzee.game.GameType
import dartzee.utils.InjectedThings.mainDatabase

/** Should be (totalRounds - 1) * 3 + (# darts in final round) */
class SanityCheckFinalScoreX01 : AbstractSanityCheckFinalScore() {
    override val gameType = GameType.X01

    override fun populateParticipantToFinalScoreTable(tempTable: String) {
        val tempTable1 =
            mainDatabase.createTempTable(
                "ParticipantToRoundCount",
                "ParticipantId VARCHAR(36), PlayerId VARCHAR(36), RoundCount INT, FinalRoundNumber INT",
            ) ?: return

        var sb = StringBuilder()
        sb.append("INSERT INTO $tempTable1")
        sb.append(
            " SELECT pt.RowId, pt.PlayerId, COUNT(DISTINCT drt.RoundNumber), MAX(drt.RoundNumber)"
        )
        sb.append(" FROM Dart drt, Participant pt, Game g")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.X01}'")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" GROUP BY pt.RowId, pt.PlayerId")

        mainDatabase.executeUpdate("" + sb)

        sb = StringBuilder()
        sb.append("INSERT INTO $tempTable")
        sb.append(" SELECT zz.ParticipantId, 3*(zz.RoundCount - 1) + COUNT(1)")
        sb.append(" FROM Dart drt, $tempTable1 zz")
        sb.append(" WHERE zz.ParticipantId = drt.ParticipantId")
        sb.append(" AND zz.PlayerId = drt.PlayerId")
        sb.append(" AND zz.FinalRoundNumber = drt.RoundNumber")
        sb.append(" GROUP BY zz.ParticipantId, zz.RoundCount")

        mainDatabase.executeUpdate("" + sb)
        mainDatabase.dropTable(tempTable1)
    }
}
