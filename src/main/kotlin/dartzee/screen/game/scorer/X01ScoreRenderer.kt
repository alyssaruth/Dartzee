package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import dartzee.`object`.DartHint
import dartzee.utils.DartsColour
import dartzee.utils.ResourceCache
import dartzee.utils.sumScore
import java.awt.Component
import java.awt.Font
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableModel

class X01ScoreRenderer : DefaultTableCellRenderer()
{
    override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        horizontalAlignment = SwingConstants.CENTER
        font = ResourceCache.BASE_FONT.deriveFont(Font.BOLD, 15f)
        val modelRow = table.convertRowIndexToModel(row)

        setColours(table, modelRow)
        return this
    }

    private fun setColours(table: JTable, modelRow: Int)
    {
        val tm = table.model
        val darts = getDartsForRow(tm, modelRow)
        if (darts.isEmpty())
        {
            foreground = null
            background = null
            return
        }

        val totalScore = sumScore(darts)

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
}