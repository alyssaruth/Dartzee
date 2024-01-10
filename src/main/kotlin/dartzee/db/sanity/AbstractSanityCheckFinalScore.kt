package dartzee.db.sanity

import dartzee.db.ParticipantEntity
import dartzee.game.GameType
import dartzee.utils.InjectedThings.mainDatabase

abstract class AbstractSanityCheckFinalScore : ISanityCheck {
    abstract val gameType: GameType

    abstract fun populateParticipantToFinalScoreTable(tempTable: String)

    override fun runCheck(): List<AbstractSanityCheckResult> {
        val tempTable =
            mainDatabase.createTempTable(
                "ParticipantToFinalScore_$gameType",
                "ParticipantId VARCHAR(36), FinalScoreCalculated INT"
            )
        tempTable ?: return listOf()

        populateParticipantToFinalScoreTable(tempTable)

        val sb = StringBuilder()
        sb.append("SELECT pt.*, zz.FinalScoreCalculated")
        sb.append(" FROM Participant pt, $tempTable zz")
        sb.append(" WHERE pt.RowId = zz.ParticipantId")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" AND pt.FinalScore <> zz.FinalScoreCalculated")

        val hmParticipantToActualCount = mutableMapOf<ParticipantEntity, Int>()
        mainDatabase.executeQuery(sb).use { rs ->
            while (rs.next()) {
                val pt = ParticipantEntity().factoryFromResultSet(rs)
                val dartCount = rs.getInt("FinalScoreCalculated")

                hmParticipantToActualCount[pt] = dartCount
            }
        }

        mainDatabase.dropTable(tempTable)

        // Add the sanity error
        if (hmParticipantToActualCount.isNotEmpty()) {
            return listOf(SanityCheckResultFinalScoreMismatch(gameType, hmParticipantToActualCount))
        }

        return listOf()
    }
}
