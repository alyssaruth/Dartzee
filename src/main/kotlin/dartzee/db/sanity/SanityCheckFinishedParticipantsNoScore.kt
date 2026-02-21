package dartzee.db.sanity

import dartzee.core.util.getEndOfTimeSqlString
import dartzee.db.ParticipantEntity

class SanityCheckFinishedParticipantsNoScore : ISanityCheck {
    override fun runCheck(): List<AbstractSanityCheckResult> {
        val sb = StringBuilder()
        sb.append(" DtFinished < ")
        sb.append(getEndOfTimeSqlString())
        sb.append(" AND FinalScore = -1")
        sb.append(" AND Resigned = false")

        val whereSql = sb.toString()
        val participants = ParticipantEntity().retrieveEntities(whereSql)
        if (participants.isNotEmpty()) {
            return listOf(
                SanityCheckResultEntitiesSimple(
                    participants,
                    "Participants marked as finished but with no final score",
                )
            )
        }

        return listOf()
    }
}
