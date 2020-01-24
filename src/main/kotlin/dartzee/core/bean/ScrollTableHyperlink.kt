package dartzee.core.bean

import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Font
import java.awt.event.MouseEvent
import java.awt.font.TextAttribute
import java.util.*
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

/**
 * A scroll table with a 'Game' column.
 * Handles rendering this column as a hyperlink to launch it on click.
 */
abstract class ScrollTableHyperlink(private val linkColumnName: String) : ScrollTable(), IHyperlinkListener
{
    private var linkColumn = -1
    private var adaptor: HyperlinkAdaptor? = null

    override var model: DefaultTableModel
        get() = super.model
        set(model) {
            super.model = model

            val linkIndex = model.findColumn(linkColumnName)
            if (linkIndex > -1) setLinkColumnIndex(linkIndex)

            //Init the adaptor if we need to, but only once
            if (adaptor == null)
            {
                adaptor = HyperlinkAdaptor(this)

                table.addMouseListener(adaptor)
                table.addMouseMotionListener(adaptor)
            }
        }

    /**
     * Allow direct setting of the game column index, so I can show game hyperlinks within the DartsScorers
     */
    fun setLinkColumnIndex(ix: Int)
    {
        linkColumn = ix
        setRenderer(linkColumn, HyperlinkRenderer(tableForeground))
    }

    override fun isOverHyperlink(arg0: MouseEvent): Boolean
    {
        val pt = arg0.point
        val col = table.columnAtPoint(pt)
        if (col != linkColumn)
        {
            return false
        }

        val row = table.rowAtPoint(pt)
        if (row == -1)
        {
            return false
        }

        val actualRow = table.convertRowIndexToModel(row)
        return table.getValueAt(actualRow, col) != null
    }

    override fun linkClicked(arg0: MouseEvent)
    {
        if (!isOverHyperlink(arg0))
        {
            return
        }

        val pt = arg0.point
        val col = table.columnAtPoint(pt)
        val row = table.rowAtPoint(pt)
        val actualRow = table.convertRowIndexToModel(row)
        val value = table.model.getValueAt(actualRow, col)

        linkClicked(value)
    }

    abstract fun linkClicked(value: Any)
    open fun renderValue(value: Any): String
    {
        return "$value"
    }


    override fun setCursor(arg0: Cursor)
    {
        super.setCursor(arg0)
        table.cursor = arg0
    }

    private inner class HyperlinkRenderer(color: Color?) : DefaultTableCellRenderer()
    {
        private var fgColor: Color? = null

        init
        {
            if (color != null)
            {
                this.fgColor = color
            } else
            {
                this.fgColor = Color.BLUE
            }
        }

        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
        {
            if (value == null)
            {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                return this
            }

            super.getTableCellRendererComponent(table, renderValue(value), isSelected, hasFocus, row, column)

            val fontAttributes = HashMap<TextAttribute, Int>()
            fontAttributes[TextAttribute.UNDERLINE] = TextAttribute.UNDERLINE_ON
            val hyperlinkFont = Font("Tahoma", Font.BOLD, 12).deriveFont(fontAttributes)

            font = hyperlinkFont

            foreground = if (isSelected) Color.WHITE else fgColor

            return this
        }
    }
}
