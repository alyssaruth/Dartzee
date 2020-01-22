package dartzee.db.sanity

import dartzee.bean.ScrollTableDartsGame
import dartzee.db.ParticipantEntity
import dartzee.utils.getTypeDesc
import dartzee.core.util.TableUtil.DefaultModel
import javax.swing.table.DefaultTableModel

class SanityCheckResultFinalScoreMismatch(private val gameType: Int, private val hmParticipantToFinalScore: MutableMap<ParticipantEntity, Int>) : AbstractSanityCheckResult()
{
    override fun getDescription() = "FinalScores that don't match the raw data (${getTypeDesc(gameType)})"

    override fun getScrollTable() = ScrollTableDartsGame("GameId")

    override fun getCount() = hmParticipantToFinalScore.size

    override fun getResultsModel(): DefaultTableModel
    {
        val model = DefaultModel()
        model.addColumn("ParticipantId")
        model.addColumn("PlayerId")
        model.addColumn("GameId")
        model.addColumn("DtLastUpdate")
        model.addColumn("FinalScore")
        model.addColumn("FinalScoreRAW")

        val pts = hmParticipantToFinalScore.keys
        for (pt in pts)
        {
            val participantId = pt.rowId
            val playerId = pt.playerId
            val gameId = pt.gameId
            val dtLastUpdate = pt.dtLastUpdate
            val finalScore = pt.finalScore
            val finalScoreRaw = hmParticipantToFinalScore[pt]!!

            val row = arrayOf(participantId, playerId, gameId, dtLastUpdate, finalScore, finalScoreRaw)
            model.addRow(row)
        }

        return model
    }

}
