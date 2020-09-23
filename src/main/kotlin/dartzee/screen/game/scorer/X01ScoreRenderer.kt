package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import dartzee.`object`.DartHint
import dartzee.utils.DartsColour
import java.awt.Component
import java.awt.Font
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableModel

class X01ScoreRenderer : DefaultTableCellRenderer()
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
        val tm = table.model
        if (getDartsForRow(tm, modelRow).isEmpty())
        {
            foreground = null
            background = null
            return
        }

        val totalScore = (getScoreAt(tm, modelRow, 0)
                + getScoreAt(tm, modelRow, 1)
                + getScoreAt(tm, modelRow, 2))

        val fg = DartsColour.getScorerForegroundColour(totalScore.toDouble())
        val bg = DartsColour.getScorerBackgroundColour(totalScore.toDouble())

        foreground = fg
        background = bg
    }

    private fun getDartsForRow(tm: TableModel, row: Int): List<Dart>
    {
        val ret = mutableListOf<Dart>()
        for (i in 0 until DartsScorerX01.SCORE_COLUMN)
        {
            val drt = tm.getValueAt(row, i) as Dart?
            if (drt != null && drt !is DartHint)
            {
                ret.add(drt)
            }
        }

        return ret
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