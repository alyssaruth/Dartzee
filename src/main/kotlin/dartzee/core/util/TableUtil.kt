package dartzee.core.util

import dartzee.core.bean.AbstractTableRenderer
import java.awt.Component
import java.awt.Font
import java.sql.Timestamp
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

object TableUtil {
    class TimestampRenderer : AbstractTableRenderer<Timestamp>() {
        override fun getReplacementValue(value: Timestamp) = value.formatTimestamp()

        override fun allowNulls() = true
    }

    class SimpleRenderer(private val alignment: Int, private val tableFont: Font?) :
        DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int,
        ): Component {
            val component =
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            if (component is JLabel) {
                component.horizontalAlignment = alignment

                if (tableFont != null) {
                    this.font = tableFont
                }
            }

            return component
        }
    }

    class DefaultModel : DefaultTableModel() {
        override fun isCellEditable(row: Int, column: Int) = false

        override fun getColumnClass(arg0: Int): Class<*> {
            return if (rowCount > 0 && getValueAt(0, arg0) != null) {
                getValueAt(0, arg0).javaClass
            } else super.getColumnClass(arg0)
        }

        fun getColumnValues(column: Int): List<Any?> {
            val list = mutableListOf<Any?>()
            for (i in 0 until rowCount) {
                list.add(getValueAt(i, column))
            }

            return list
        }

        fun setColumnNames(cols: List<String>) {
            cols.forEach { addColumn(it) }
        }

        fun <T : Any> addRows(rows: Collection<Array<T>>) {
            rows.forEach { addRow(it) }
        }

        fun clear() {
            while (rowCount > 0) {
                removeRow(0)
            }
        }
    }
}
