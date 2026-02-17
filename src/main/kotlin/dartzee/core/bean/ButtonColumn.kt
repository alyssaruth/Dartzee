package dartzee.core.bean

import dartzee.bean.IMouseListener
import java.awt.Color
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import javax.swing.AbstractCellEditor
import javax.swing.Action
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.border.Border
import javax.swing.border.LineBorder
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

/**
 * The ButtonColumn class provides a renderer and an editor that looks like a JButton. The renderer
 * and editor will then be used for a specified column in the table. The TableModel will contain the
 * String to be displayed on the button.
 *
 * The button can be invoked by a mouse click or by pressing the space bar when the cell has focus.
 * Optionally a mnemonic can be set to invoke the button. When the button is invoked the provided
 * Action is invoked. The source of the Action will be the table. The action command will contain
 * the model row number of the button that was clicked.
 */
class ButtonColumn(private val table: ScrollTable, private val action: Action, column: Int) :
    AbstractCellEditor(), TableCellRenderer, TableCellEditor, ActionListener, IMouseListener {
    private val originalBorder: Border

    private val renderButton = JButton()
    val editButton = JButton()
    private var editorValue: Any? = null
    private var isButtonColumnEditor: Boolean = false

    init {
        editButton.isFocusPainted = false
        editButton.addActionListener(this)
        originalBorder = editButton.border
        editButton.border = LineBorder(Color.BLUE)

        val columnModel = table.columnModel
        columnModel.getColumn(column).cellRenderer = this
        columnModel.getColumn(column).cellEditor = this
        table.addMouseListener(this)
    }

    override fun getTableCellEditorComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int,
    ): Component {
        when (value) {
            null -> editButton.clear()
            is Icon -> editButton.icon = value
            else -> editButton.text = "$value"
        }

        this.editorValue = value
        return editButton
    }

    private fun JButton.clear() {
        text = ""
        icon = null
    }

    override fun getCellEditorValue() = editorValue

    /** TableCellRenderer */
    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        if (isSelected) {
            renderButton.foreground = table.selectionForeground
            renderButton.background = table.selectionBackground
        } else {
            renderButton.foreground = table.foreground
            renderButton.background = UIManager.getColor("Button.background")
        }

        renderButton.border = if (hasFocus) LineBorder(Color.BLUE) else originalBorder

        table.rowHeight = 40

        when (value) {
            null -> renderButton.clear()
            is Icon -> renderButton.icon = value
            else -> renderButton.text = "$value"
        }

        return renderButton
    }

    /** ActionListener */
    override fun actionPerformed(e: ActionEvent) {
        val row = table.convertRowIndexToModel(table.editingRow)
        fireEditingStopped()

        //  Invoke the Action
        val event = ActionEvent(table, ActionEvent.ACTION_PERFORMED, "$row")
        action.actionPerformed(event)
    }

    /**
     * When the mouse is pressed the editor is invoked. If you then drag the mouse to another cell
     * before releasing it, the editor is still active. Make sure editing is stopped when the mouse
     * is released.
     */
    override fun mousePressed(e: MouseEvent) {
        if (table.isEditing && table.cellEditor === this) isButtonColumnEditor = true
    }

    override fun mouseReleased(e: MouseEvent) {
        if (isButtonColumnEditor && table.isEditing) table.cellEditor.stopCellEditing()

        isButtonColumnEditor = false
    }
}
