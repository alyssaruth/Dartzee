package dartzee.screen.game

import dartzee.utils.DartsColour
import dartzee.utils.InjectedThings
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JTable
import javax.swing.border.MatteBorder
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableModel

class GameStatisticsCellRenderer(
    private val sectionStarts: List<String>,
    private val highestWins: List<String>,
    private val lowestWins: List<String>,
    private val histogramRows: List<String>,
) : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        table ?: return this

        horizontalAlignment = CENTER

        val style = if (column == 0) Font.BOLD else Font.PLAIN
        font = Font("Trebuchet MS", style, 15)

        setColours(table, row, column)
        border = getBorder(table, row, column)

        return this
    }

    private fun getBorder(table: JTable, row: Int, column: Int): MatteBorder {
        val left = if (column == 0) 2 else 1
        val right = if (column == table.model.columnCount - 1) 2 else 1
        val bottom = if (row == table.rowCount - 1) 2 else 0

        val startOfSectionRow = sectionStarts.contains(table.getValueAt(row, 0))
        val top = if (startOfSectionRow) 2 else 0

        return MatteBorder(top, left, bottom, right, Color.BLACK)
    }

    private fun setColours(table: JTable, row: Int, column: Int) {
        val white = InjectedThings.theme?.lightBackground ?: Color.WHITE
        if (column == 0) {
            // Do nothing
            foreground = null
            background = white
            return
        }

        val tm = table.model

        val rowName = table.getValueAt(row, 0)
        if (highestWins.contains(rowName)) {
            val pos = getPositionForColour(tm, row, column, true)
            DartsColour.setFgAndBgColoursForPosition(this, pos, Color.WHITE)
        } else if (lowestWins.contains(rowName)) {
            val pos = getPositionForColour(tm, row, column, false)
            DartsColour.setFgAndBgColoursForPosition(this, pos, Color.WHITE)
        } else if (histogramRows.contains(rowName)) {
            val sum = getHistogramSum(tm, column)
            val baseColor =
                InjectedThings.theme?.primary ?: Color.getHSBColor(0.5.toFloat(), 1f, 1f)
            val hsbValues =
                Color.RGBtoHSB(baseColor.red, baseColor.green, baseColor.blue, FloatArray(3))
            val thisValue = getDoubleAt(tm, row, column)
            val percent = if (sum == 0L) 0f else thisValue.toFloat() / sum

            val bg = Color.getHSBColor(hsbValues[0], percent, hsbValues[2])

            foreground = null
            background = bg
        } else {
            foreground = null
            background = white
        }
    }

    private fun getDoubleAt(tm: TableModel, row: Int, col: Int): Double {
        val thisValue = tm.getValueAt(row, col)

        if (thisValue == null) {
            return -1.0
        }

        return (thisValue as Number).toDouble()
    }

    private fun getPositionForColour(
        tm: TableModel,
        row: Int,
        col: Int,
        highestWins: Boolean,
    ): Int {
        if (tm.getValueAt(row, col) is String || tm.columnCount == 2) {
            return -1
        }

        val myScore = getDoubleAt(tm, row, col)

        var myPosition = 1
        for (i in 1 until tm.columnCount) {
            if (i == col || tm.getValueAt(row, i) is String) {
                continue
            }

            val theirScore = getDoubleAt(tm, row, i)

            // Compare positivity to the boolean
            val result = theirScore.compareTo(myScore)
            if (result > 0 == highestWins && result != 0) {
                myPosition++
            }
        }

        return myPosition
    }

    private fun getHistogramSum(tm: TableModel, col: Int) =
        getHistogramRowNumbers(tm).sumOf { row -> (tm.getValueAt(row, col) as Number).toLong() }

    private fun getHistogramRowNumbers(tm: TableModel) =
        (0 until tm.rowCount).filter { histogramRows.contains(tm.getValueAt(it, 0)) }
}
