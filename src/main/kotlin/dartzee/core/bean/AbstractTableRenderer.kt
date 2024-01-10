package dartzee.core.bean

import dartzee.logging.CODE_RENDER_ERROR
import dartzee.utils.InjectedThings.logger
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

/**
 * Abstract extension of DefaultTableCellRenderer to allow parameterisation using generics. Also
 * provides a place to do extra checks (e.g. null values).
 */
abstract class AbstractTableRenderer<E> : DefaultTableCellRenderer() {
    abstract fun getReplacementValue(value: E): Any

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        try {
            @Suppress("UNCHECKED_CAST") val typedValue = value as E?

            val newValue = getReplacementValue(typedValue, row, column)
            super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column)

            setFontsAndAlignment()
            setCellColours(typedValue, isSelected)

            val rowHeight = getRowHeight()
            if (rowHeight > -1) {
                table?.rowHeight = rowHeight
            }

            // For ButtonRenderer. If we're actually a component, then return the component itself
            // (otherwise we just call toString()
            // on w/e object it is, which doesn't work)
            if (newValue is Component) {
                return newValue
            }
        } catch (t: Throwable) {
            logger.error(
                CODE_RENDER_ERROR,
                "Error rendering row [$row], col [$column]. Value [$value]",
                t
            )
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        }

        return this
    }

    private fun getReplacementValue(typedValue: E?, row: Int, column: Int): Any? {
        if (typedValue == null) {
            if (!allowNulls()) {
                logger.error(
                    CODE_RENDER_ERROR,
                    "NULL element in table at row [$row] and column [$column]"
                )
            }

            return ""
        }

        return getReplacementValue(typedValue)
    }

    /** Default methods */
    open fun allowNulls() = false

    open fun getRowHeight() = -1

    open fun setCellColours(typedValue: E?, isSelected: Boolean) {
        // do nothing
    }

    open fun setFontsAndAlignment() {
        // do nothing
    }
}
