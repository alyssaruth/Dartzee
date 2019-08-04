package burlton.dartzee.code.db.sanity

import burlton.dartzee.code.db.ParticipantEntity
import burlton.desktopcore.code.util.getEndOfTimeSqlString

class SanityCheckFinishedParticipantsNoScore: AbstractSanityCheck()
{
    override fun runCheck(): List<AbstractSanityCheckResult>
    {
        val sb = StringBuilder()
        sb.append(" DtFinished < ")
        sb.append(getEndOfTimeSqlString())
        sb.append(" AND FinalScore = -1")

        val whereSql = sb.toString()
        val participants = ParticipantEntity().retrieveEntities(whereSql)
        if (!participants.isEmpty())
        {
            return listOf(SanityCheckResultEntitiesSimple(participants, "Participants marked as finished but with no final score"))
        }

        return listOf()
    }
}