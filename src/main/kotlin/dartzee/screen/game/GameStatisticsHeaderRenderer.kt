package dartzee.screen.game

import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JTable
import javax.swing.JTextPane
import javax.swing.border.MatteBorder
import javax.swing.table.TableCellRenderer
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class GameStatisticsHeaderRenderer: JTextPane(), TableCellRenderer
{
    init
    {
        val doc = this.styledDocument
        val center = SimpleAttributeSet()
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER)
        doc.setParagraphAttributes(0, doc.length, center, false)
    }

    override fun getTableCellRendererComponent(table: JTable,
                                               value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int,
                                               column: Int): Component
    {
        text = value as String
        font = Font("Trebuchet MS", Font.BOLD, 15)
        border = getBorder(table, column)

        setSize(table.columnModel.getColumn(column).width, preferredSize.height)

        if (column == 0)
        {
            background = Color(0, 0, 0, 0)
            isOpaque = false
        }

        return this
    }

    private fun getBorder(table: JTable, column: Int): MatteBorder
    {
        val top = if (column == 0) 0 else 2
        val left = if (column == 0) 0 else 1
        val right = if (column == table.columnCount - 1) 2 else 1

        return MatteBorder(top, left, 2, right, Color.BLACK)
    }
}