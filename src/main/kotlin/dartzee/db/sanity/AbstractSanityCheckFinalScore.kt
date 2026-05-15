package dartzee.db.sanity

import dartzee.core.util.TableUtil.DefaultModel
import dartzee.db.ParticipantEntity
import dartzee.game.GameType
import dartzee.utils.InjectedThings.mainDatabase
import javax.swing.table.DefaultTableModel

abstract class AbstractSanityCheckFinalScore : ISanityCheck {
    abstract val gameType: GameType

    abstract fun populateParticipantToFinalScoreTable(tempTable: String)

    override fun runCheck(): List<SanityCheckResult> {
        val tempTable =
            mainDatabase.createTempTable(
                "ParticipantToFinalScore_$gameType",
                "ParticipantId VARCHAR(36), FinalScoreCalculated INT",
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
            return listOf(
                SanityCheckResultFinalScoreMismatch(
                    gameType,
                    buildResultsModel(hmParticipantToActualCount),
                )
            )
        }

        return listOf()
    }

    private fun buildResultsModel(
        hmParticipantToActualCount: Map<ParticipantEntity, Int>
    ): DefaultTableModel {
        val model = DefaultModel()
        model.addColumn("ParticipantId")
        model.addColumn("PlayerId")
        model.addColumn("GameId")
        model.addColumn("DtLastUpdate")
        model.addColumn("FinalScore")
        model.addColumn("FinalScoreRAW")

        val pts = hmParticipantToActualCount.keys
        for (pt in pts) {
            val participantId = pt.rowId
            val playerId = pt.playerId
            val gameId = pt.gameId
            val dtLastUpdate = pt.dtLastUpdate
            val finalScore = pt.finalScore
            val finalScoreRaw = hmParticipantToActualCount[pt]!!

            val row =
                arrayOf<Any>(
                    participantId,
                    playerId,
                    gameId,
                    dtLastUpdate,
                    finalScore,
                    finalScoreRaw,
                )
            model.addRow(row)
        }

        return model
    }
}
