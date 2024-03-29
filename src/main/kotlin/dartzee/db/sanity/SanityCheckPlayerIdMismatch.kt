package dartzee.db.sanity

import dartzee.core.util.TableUtil
import dartzee.utils.InjectedThings.mainDatabase

class SanityCheckPlayerIdMismatch : ISanityCheck {
    override fun runCheck(): List<AbstractSanityCheckResult> {
        val sb = StringBuilder()

        sb.append(
            "SELECT drt.RowId AS DartId, pt.RowId AS ParticipantId, drt.PlayerId AS DartPlayerId, pt.PlayerId as ParticipantPlayerId"
        )
        sb.append(" FROM Dart drt, Participant pt")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId <> pt.PlayerId")

        val tm = TableUtil.DefaultModel()
        tm.addColumn("DartId")
        tm.addColumn("ParticipantId")
        tm.addColumn("DartPlayerId")
        tm.addColumn("ParticipantPlayerId")

        val rows =
            mainDatabase.retrieveAsList(sb) { rs ->
                val dartId = rs.getString("DartId")
                val participantId = rs.getString("ParticipantId")
                val dartPlayerId = rs.getString("DartPlayerId")
                val participantPlayerId = rs.getString("ParticipantPlayerId")

                arrayOf(dartId, participantId, dartPlayerId, participantPlayerId)
            }

        tm.addRows(rows)

        if (tm.rowCount > 0) {
            return listOf(
                SanityCheckResultSimpleTableModel(
                    tm,
                    "Darts where PlayerId doesn't match the Participant row"
                )
            )
        }

        return listOf()
    }
}
