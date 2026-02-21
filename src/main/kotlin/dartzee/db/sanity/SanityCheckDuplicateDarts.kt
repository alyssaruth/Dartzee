package dartzee.db.sanity

import dartzee.db.DartEntity

class SanityCheckDuplicateDarts : ISanityCheck {
    override fun runCheck(): List<AbstractSanityCheckResult> {
        val sb = StringBuilder()
        sb.append(" EXISTS (")
        sb.append(" SELECT 1")
        sb.append(" FROM Dart drt2")
        sb.append(" WHERE drt.PlayerId = drt2.PlayerId")
        sb.append(" AND drt.ParticipantId = drt2.ParticipantId")
        sb.append(" AND drt.RoundNumber = drt2.RoundNumber")
        sb.append(" AND drt.Ordinal = drt2.Ordinal")
        sb.append(" AND drt.RowId <> drt2.RowId")
        sb.append(")")

        val whereSql = sb.toString()
        val darts = DartEntity().retrieveEntities(whereSql, "drt")
        if (!darts.isEmpty()) {
            return listOf(SanityCheckResultEntitiesSimple(darts, "Duplicate darts"))
        }

        return listOf()
    }
}
