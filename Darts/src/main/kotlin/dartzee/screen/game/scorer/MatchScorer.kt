package dartzee.screen.game.scorer

import dartzee.db.DartsMatchEntity
import dartzee.db.ParticipantEntity
import dartzee.utils.DartsColour
import dartzee.core.bean.AbstractTableRenderer

/**
 * For the 'Match Summary' tab.
 */
class MatchScorer : AbstractScorer()
{
    private var match: DartsMatchEntity? = null

    /**
     * Game #, Score, Position, Points
     */
    override fun getNumberOfColumns() = 4

    override fun initImpl(gameParams: String)
    {
        tableScores.setLinkColumnIndex(0)

        for (i in COLUMN_NO_GAME_ID + 1 until model.columnCount)
        {
            tableScores.getColumn(i).cellRenderer = ParticipantRenderer(i)
        }

        tableScores.setColumnWidths("100")
    }

    fun setMatch(match: DartsMatchEntity)
    {
        this.match = match
    }

    fun updateResult()
    {
        var totalScore = 0

        val rowCount = tableScores.rowCount
        for (i in 0 until rowCount)
        {
            val pt = tableScores.getValueAt(i, COLUMN_NO_MATCH_POINTS) as ParticipantEntity
            totalScore += match!!.getScoreForFinishingPosition(pt.finishingPosition)
        }

        lblResult.isVisible = true
        lblResult.text = "" + totalScore

        //Also update the screen
        tableScores.repaint()
    }

    /**
     * Inner classes
     */
    private inner class ParticipantRenderer(private val colNo: Int) : AbstractTableRenderer<ParticipantEntity>()
    {
        override fun getReplacementValue(value: ParticipantEntity): Any
        {
            return when (colNo)
            {
                COLUMN_NO_FINAL_SCORE -> if (value.finalScore == -1) "N/A" else value.finalScore
                COLUMN_NO_FINISHING_POSITION -> value.getFinishingPositionDesc()
                COLUMN_NO_MATCH_POINTS -> match!!.getScoreForFinishingPosition(value.finishingPosition)
                else -> ""
            }
        }

        override fun setCellColours(typedValue: ParticipantEntity?, isSelected: Boolean)
        {
            if (colNo == COLUMN_NO_FINISHING_POSITION)
            {
                val finishingPos = typedValue!!.finishingPosition
                DartsColour.setFgAndBgColoursForPosition(this, finishingPos)
            }
        }
    }

    companion object
    {
        private const val COLUMN_NO_GAME_ID = 0
        private const val COLUMN_NO_FINAL_SCORE = 1
        private const val COLUMN_NO_FINISHING_POSITION = 2
        private const val COLUMN_NO_MATCH_POINTS = 3
    }
}
