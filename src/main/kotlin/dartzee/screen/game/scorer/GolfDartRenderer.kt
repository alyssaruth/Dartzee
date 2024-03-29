package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings.preferenceService
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.border.Border
import javax.swing.border.MatteBorder
import javax.swing.table.DefaultTableCellRenderer

class GolfDartRenderer(private val showGameId: Boolean) : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val newValue = getReplacementValue(table, value, row)
        val cell =
            super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column)
                as JComponent

        horizontalAlignment = SwingConstants.CENTER
        font = Font("Trebuchet MS", Font.BOLD, 15)

        val border = getBorderForCell(row, column)
        cell.border = border

        if (column == 0 || newValue == null || isScoreRow(row)) {
            foreground = null
            background = null
        } else {
            val score = newValue as Int

            val bgBrightness = preferenceService.get(Preferences.bgBrightness)
            val fgBrightness = preferenceService.get(Preferences.fgBrightness)

            foreground = getGolfScorerColour(score, fgBrightness)
            background = getGolfScorerColour(score, bgBrightness)
        }

        return this
    }

    private fun getReplacementValue(table: JTable?, obj: Any?, row: Int): Any? {
        if (obj == null) {
            return null
        }

        if (obj !is Dart) {
            return obj
        }

        val target = table!!.getValueAt(row, 0) as Int
        return obj.getGolfScore(target)
    }

    private fun getBorderForCell(row: Int, col: Int): Border {
        var top = 0
        var bottom = 0
        var left = 0
        var right = 0

        if (isScoreRow(row)) {
            top = 2
            bottom = 2
        }

        if (col == 1) {
            left = 2
        }

        if (col == 3) {
            right = 2
        }

        if (showGameId && col == 4) {
            right = 2
        }

        return MatteBorder(top, left, bottom, right, Color.BLACK)
    }
}
