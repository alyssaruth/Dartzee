package burlton.desktopcore.code.util

import burlton.core.code.util.Debug
import java.awt.Component
import java.awt.Font
import java.sql.Timestamp
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

object TableUtil
{
    class TimestampRenderer: DefaultTableCellRenderer()
    {
        override fun getTableCellRendererComponent(table: JTable?, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
        {
            val newValue = getObjectForValue(value, row, column)
            super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column)
            return this
        }

        private fun getObjectForValue(value: Any?, row: Int, column: Int): Any?
        {
            if (value == null)
            {
                return ""
            }

            if (value !is Timestamp)
            {
                Debug.stackTrace("Non-timestamp object in table. Row $row, Col $column")
                return null
            }

            val timestamp = value as Timestamp?
            return timestamp!!.formatTimestamp()
        }
    }

    class SimpleRenderer(private val alignment: Int, private val tableFont: Font?) : DefaultTableCellRenderer()
    {
        override fun getTableCellRendererComponent(table: JTable?, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
        {
            val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            if (component is JLabel)
            {
                component.horizontalAlignment = alignment

                if (tableFont != null)
                {
                    this.font = tableFont
                }
            }

            return component
        }
    }

    class DefaultModel : DefaultTableModel()
    {
        override fun isCellEditable(row: Int, column: Int) = false

        override fun getColumnClass(arg0: Int): Class<*>
        {
            return if (rowCount > 0 && getValueAt(0, arg0) != null)
            {
                getValueAt(0, arg0).javaClass
            }
            else super.getColumnClass(arg0)
        }

        fun addRows(rows: Collection<Array<Any>>)
        {
            rows.forEach { addRow(it) }
        }
    }
}
