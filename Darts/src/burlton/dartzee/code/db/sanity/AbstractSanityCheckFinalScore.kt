package burlton.dartzee.code.db.sanity

import burlton.dartzee.code.db.ParticipantEntity
import burlton.dartzee.code.utils.DatabaseUtil

abstract class AbstractSanityCheckFinalScore: AbstractSanityCheck()
{
    abstract val gameType: Int

    abstract fun populateParticipantToFinalScoreTable(tempTable: String)

    override fun runCheck(): List<AbstractSanityCheckResult>
    {
        val tempTable = DatabaseUtil.createTempTable("ParticipantToFinalScore_$gameType", "ParticipantId VARCHAR(36), FinalScoreCalculated INT")
        tempTable ?: return listOf()

        populateParticipantToFinalScoreTable(tempTable)

        val sb = StringBuilder()
        sb.append("SELECT pt.*, zz.FinalScoreCalculated")
        sb.append(" FROM Participant pt, $tempTable zz")
        sb.append(" WHERE pt.RowId = zz.ParticipantId")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" AND pt.FinalScore <> zz.FinalScoreCalculated")

        val hmParticipantToActualCount = mutableMapOf<ParticipantEntity, Int>()
            DatabaseUtil.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val pt = ParticipantEntity().factoryFromResultSet(rs)
                    val dartCount = rs.getInt("FinalScoreCalculated")

                    hmParticipantToActualCount[pt] = dartCount
                }
            }

        DatabaseUtil.dropTable(tempTable)

        //Add the sanity error
        if (!hmParticipantToActualCount.isEmpty())
        {
            return listOf(SanityCheckResultFinalScoreMismatch(gameType, hmParticipantToActualCount))
        }

        return listOf()
    }
}