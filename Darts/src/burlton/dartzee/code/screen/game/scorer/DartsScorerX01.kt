package burlton.dartzee.code.screen.game.scorer

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartHint
import burlton.dartzee.code.screen.game.GamePanelPausable
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.code.utils.sumScore
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableModel

class DartsScorerX01(parent: GamePanelPausable<out DartsScorerPausable>) : DartsScorerPausable(parent)
{
    private val lblStartingScore = JLabel("X01")

    fun getLatestScoreRemaining(): Int
    {
        val model = tableScores.model

        val rowCount = model.rowCount
        return getLatestScoreRemaining(rowCount)
    }
    private fun getLatestScoreRemaining(rowCount: Int): Int
    {
        return if (rowCount == 0)
        {
            Integer.parseInt(lblStartingScore.text)
        }
        else
        {
            val currentRow = model.getValueAt(rowCount - 1, SCORE_COLUMN) as Int?
            currentRow ?: getLatestScoreRemaining(rowCount - 1)
        }
    }

    init
    {
        lblStartingScore.horizontalAlignment = SwingConstants.CENTER
        lblStartingScore.font = Font("Trebuchet MS", Font.PLAIN, 16)
        panelNorth.add(lblStartingScore, BorderLayout.CENTER)
    }


    override fun initImpl(gameParams: String)
    {
        val startingScore = Integer.parseInt(gameParams)
        lblStartingScore.text = "$startingScore"

        tableScores.getColumn(SCORE_COLUMN).cellRenderer = ScorerRenderer()
        for (i in 0 until SCORE_COLUMN)
        {
            tableScores.getColumn(i).cellRenderer = DartRenderer()
        }
    }

    override fun playerIsFinished() = getLatestScoreRemaining() == 0

    /**
     * How many darts have been thrown?
     *
     * 3 * (rows - 1) + #(darts in the last row)
     */
    override fun getTotalScore(): Int
    {
        val rowCount = model.rowCount
        if (rowCount == 0)
        {
            return 0
        }

        var dartCount = Math.max((model.rowCount - 1) * 3, 0)

        //We now use this mid-game
        if (rowIsComplete(rowCount - 1) && !playerIsFinished())
        {
            return dartCount + 3
        }

        dartCount += getDartsForRow(rowCount - 1).size
        return dartCount
    }

    fun getDartsForRow(row: Int): List<Dart>
    {
        val ret = mutableListOf<Dart>()
        for (i in 0 until SCORE_COLUMN)
        {
            val drt = model.getValueAt(row, i) as Dart?
            if (drt != null && drt !is DartHint)
            {
                ret.add(drt)
            }
        }

        return ret
    }

    override fun rowIsComplete(rowNumber: Int) = model.getValueAt(rowNumber, SCORE_COLUMN) != null

    override fun getNumberOfColumns() = SCORE_COLUMN + 1

    fun finaliseRoundScore(startingScore: Int, bust: Boolean)
    {
        removeHints()

        val row = model.rowCount - 1

        if (bust)
        {
            model.setValueAt(startingScore, row, SCORE_COLUMN)
        }
        else
        {
            val dartScore = sumScore(getDartsForRow(row))
            model.setValueAt(startingScore - dartScore, row, SCORE_COLUMN)
        }
    }

    override fun addDart(drt: Dart)
    {
        removeHints()

        super.addDart(drt)
    }

    fun addHint(drt: DartHint)
    {
        super.addDart(drt)
    }

    private fun removeHints()
    {
        val row = model.rowCount - 1
        if (row < 0)
        {
            return
        }

        for (i in 0 until SCORE_COLUMN)
        {
            if (model.getValueAt(row, i) is DartHint)
            {
                model.setValueAt(null, row, i)
            }
        }
    }

    private inner class ScorerRenderer : DefaultTableCellRenderer()
    {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            horizontalAlignment = SwingConstants.CENTER
            font = Font("Trebuchet MS", Font.BOLD, 15)
            val modelRow = table!!.convertRowIndexToModel(row)

            setColours(table, modelRow)
            return this
        }

        private fun setColours(table: JTable, modelRow: Int)
        {
            if (getDartsForRow(modelRow).isEmpty())
            {
                foreground = null
                background = null
                return
            }

            val tm = table.model
            val totalScore = (getScoreAt(tm, modelRow, 0)
                    + getScoreAt(tm, modelRow, 1)
                    + getScoreAt(tm, modelRow, 2))

            val fg = DartsColour.getScorerForegroundColour(totalScore.toDouble())
            val bg = DartsColour.getScorerBackgroundColour(totalScore.toDouble())

            foreground = fg
            background = bg
        }

        private fun getScoreAt(tm: TableModel, row: Int, col: Int): Int
        {
            val value = tm.getValueAt(row, col) as Dart? ?: return 0
            if (value is DartHint)
            {
                return 0
            }

            return value.getTotal()
        }
    }

    companion object
    {
        private const val SCORE_COLUMN = 3
    }
}
