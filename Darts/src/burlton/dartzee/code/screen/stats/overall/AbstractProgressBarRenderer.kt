package burlton.dartzee.code.screen.stats.overall

import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JProgressBar
import javax.swing.JTable
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import javax.swing.plaf.basic.BasicProgressBarUI
import javax.swing.table.TableCellRenderer

abstract class AbstractProgressBarRenderer: JProgressBar(), TableCellRenderer
{
    init
    {
        font = Font("Tahoma", Font.BOLD, 12)
        minimum = 0
        isStringPainted = true

        val borderMargin = EmptyBorder(1, 1, 1, 1)
        val lineBorder = MatteBorder(1, 1, 1, 1, Color.BLACK)
        border = CompoundBorder(borderMargin, lineBorder)

        setUI(AchievementProgressUI())
    }

    abstract fun getColorForValue(value: Any?): Color
    abstract fun getScoreForValue(value: Any?): Int

    override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
    {
        val score = getScoreForValue(value)
        string = getScoreDescForValue(value)

        setValue(score)

        var col = getColorForValue(value)
        if (isSelected)
        {
            col = col.darker()
        }

        foreground = col.darker()
        background = col

        return this
    }

    open fun getScoreDescForValue(value: Any?) = "${getScoreForValue(value)}/$maximum"

    private class AchievementProgressUI : BasicProgressBarUI()
    {
        override fun getSelectionBackground(): Color
        {
            return progressBar.foreground
        }

        override fun getSelectionForeground(): Color
        {
            return progressBar.foreground.darker()
        }
    }
}