package dartzee.screen.game.scorer

import dartzee.core.bean.AbstractTableRenderer
import dartzee.db.DartsMatchEntity
import dartzee.game.state.IWrappedParticipant
import dartzee.utils.DartsColour

/** For the 'Match Summary' tab. */
class MatchScorer(pt: IWrappedParticipant, private val match: DartsMatchEntity) :
    AbstractScorer(pt) {
    /** Game #, Score, Position, Points */
    override fun getNumberOfColumns() = 4

    override fun initImpl() {
        tableScores.setLinkColumnIndex(0)

        for (i in COLUMN_NO_GAME_ID + 1 until model.columnCount) {
            tableScores.getColumn(i).cellRenderer = ParticipantRenderer(i)
        }

        tableScores.setColumnWidths("100")
    }

    fun updateResult() {
        var totalScore = 0

        val rowCount = tableScores.rowCount
        for (i in 0 until rowCount) {
            val pt = tableScores.getNonNullValueAt(i, COLUMN_NO_MATCH_POINTS) as IWrappedParticipant
            totalScore += match.getScoreForFinishingPosition(pt.participant.finishingPosition)
        }

        lblResult.isVisible = true
        lblResult.text = "" + totalScore

        // Also update the screen
        tableScores.repaint()
    }

    /** Inner classes */
    private inner class ParticipantRenderer(private val colNo: Int) :
        AbstractTableRenderer<IWrappedParticipant>() {
        override fun getReplacementValue(value: IWrappedParticipant): Any {
            val pt = value.participant
            return when (colNo) {
                COLUMN_NO_FINAL_SCORE -> if (pt.finalScore == -1) "N/A" else pt.finalScore
                COLUMN_NO_FINISHING_POSITION -> pt.getFinishingPositionDesc()
                COLUMN_NO_MATCH_POINTS -> match.getScoreForFinishingPosition(pt.finishingPosition)
                else -> ""
            }
        }

        override fun setCellColours(typedValue: IWrappedParticipant?, isSelected: Boolean) {
            if (colNo == COLUMN_NO_FINISHING_POSITION) {
                val finishingPos = typedValue!!.participant.finishingPosition
                DartsColour.setFgAndBgColoursForPosition(this, finishingPos)
            }
        }
    }

    companion object {
        private const val COLUMN_NO_GAME_ID = 0
        private const val COLUMN_NO_FINAL_SCORE = 1
        private const val COLUMN_NO_FINISHING_POSITION = 2
        private const val COLUMN_NO_MATCH_POINTS = 3
    }
}
