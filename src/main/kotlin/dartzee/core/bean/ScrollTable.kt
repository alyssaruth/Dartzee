package dartzee.core.bean

import dartzee.bean.IMouseListener
import dartzee.core.util.TableUtil.DefaultModel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Rectangle
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.border.AbstractBorder
import javax.swing.border.EmptyBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.event.TableColumnModelEvent
import javax.swing.event.TableColumnModelListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn
import javax.swing.table.TableColumnModel
import javax.swing.table.TableModel
import javax.swing.table.TableRowSorter

open class ScrollTable(val testId: String = "") : JPanel(), TableColumnModelListener,
    ListSelectionListener, IMouseListener
{
    private var rowNameSingular = "row"
    private var rowNamePlural: String? = null
    private var fgColor: Color? = null
    private var sortingEnabled = true
    private var rowSorter: TableRowSorter<TableModel>? = null

    private val listeners = ArrayList<RowSelectionListener>()
    private val clickListeners = ArrayList<IDoubleClickListener>()

    private val scrollPane = JScrollPane()
    val table: JTable = object : JTable() {
        override fun isCellEditable(arg0: Int, arg1: Int) = isEditable(arg0, arg1)
    }
    val lblRowCount = JLabel("<Row Count>")
    private val panelRowCount = JPanel()
    private val tableFooter: JTable = object : JTable() {
        override fun isCellEditable(row: Int, column: Int) = false
    }
    private val panelCenter = JPanel()

    init
    {
        layout = BorderLayout(0, 0)
        table.columnModel.addColumnModelListener(this)
        table.addMouseListener(this)
        table.selectionModel.addListSelectionListener(this)
        tableFooter.selectionModel.addListSelectionListener(this)
        add(panelCenter, BorderLayout.CENTER)
        panelCenter.layout = BorderLayout(0, 0)
        panelCenter.add(scrollPane, BorderLayout.CENTER)
        scrollPane.setViewportView(table)
        table.tableHeader.reorderingAllowed = false
        table.fillsViewportHeight = true
        panelCenter.add(panelRowCount, BorderLayout.SOUTH)
        panelRowCount.layout = BorderLayout(0, 0)
        panelRowCount.add(tableFooter, BorderLayout.CENTER)
        panelRowCount.add(lblRowCount, BorderLayout.SOUTH)
        lblRowCount.horizontalAlignment = SwingConstants.RIGHT
        lblRowCount.border = EmptyBorder(0, 10, 0, 10)
        tableFooter.tableHeader.reorderingAllowed = false
        tableFooter.fillsViewportHeight = true
        tableFooter.isVisible = false
        val boldFont = tableFooter.font.deriveFont(Font.BOLD)
        tableFooter.font = boldFont

        addKeyAction(KeyEvent.VK_DOWN) {
            val row = table.selectedRow
            if (row == table.rowCount - 1
                && tableFooter.isVisible
            ) {
                selectRow(TABLE_ROW_FOOTER)
                tableFooter.requestFocus()
            } else if (row < table.rowCount - 1) {
                selectRow(row + 1)
            }
        }

        tableFooter.addKeyAction(KeyEvent.VK_UP) {
            selectRow(table.rowCount - 1)
            table.requestFocus()
        }
    }

    //Initialise our footer model in preparation
    open var model: DefaultTableModel
        get() = table.model as DefaultTableModel
        set(model) {
            table.model = model
            if (sortingEnabled) {
                rowSorter = TableRowSorter(model)
                table.rowSorter = rowSorter
            }
            //Initialise our footer model in preparation
            val footerModel = DefaultModel()
            for (i in 0 until model.columnCount) {
                footerModel.addColumn("")
            }
            tableFooter.model = footerModel
            refreshRowCount()
        }

    fun addRow(row: List<*>)
    {
        addRow(row.toTypedArray())
    }
    fun addRow(row: Array<*>)
    {
        model.addRow(row)
        refreshRowCount()
    }

    fun insertRow(row: Array<Any?>?, index: Int)
    {
        model.insertRow(index, row)
        refreshRowCount()
    }

    fun addColumn(columnName: String?)
    {
        model.addColumn(columnName)
        val footerModel = tableFooter.model as DefaultTableModel
        footerModel.addColumn(columnName)
    }

    private fun refreshRowCount()
    {
        val rows = table.rowCount
        val rowCountDesc = getRowCountDesc(rows)
        lblRowCount.text = rowCountDesc
    }

    private fun getRowCountDesc(rows: Int): String
    {
        val rowName = when
        {
            rows == 1 -> rowNameSingular
            rowNamePlural != null -> rowNamePlural
            else -> "${rowNameSingular}s"
        }

        return "$rows $rowName"
    }

    fun setRowName(rowNameSingular: String, rowNamePlural: String? = null)
    {
        this.rowNameSingular = rowNameSingular
        this.rowNamePlural = rowNamePlural
    }

    fun setShowRowCount(show: Boolean)
    {
        panelRowCount.isVisible = show
    }

    fun setRowCountAlignment(alignment: Int)
    {
        lblRowCount.horizontalAlignment = alignment
    }

    val rowCount: Int
        get() = table.rowCount

    val columnCount: Int
        get() = table.columnCount

    fun getColumnName(colIndex: Int): String = table.getColumnName(colIndex)

    val selectionModel: ListSelectionModel
        get() = table.selectionModel

    fun setSelectionMode(mode: Int) {
        table.setSelectionMode(mode)
    }

    fun setRowHeight(height: Int) {
        table.rowHeight = height
    }

    fun getValueAt(row: Int, col: Int): Any? = when (row) {
        TABLE_ROW_FOOTER -> tableFooter.getValueAt(0, col)
        else -> model.getValueAt(row, col)
    }

    fun setPreferredScrollableViewportSize(dim: Dimension?) {
        table.preferredScrollableViewportSize = dim
    }

    var tableForeground: Color?
        get() = fgColor
        set(color) {
            fgColor = color
            table.foreground = color
            tableFooter.foreground = color
        }

    fun setFillsViewportHeight(fill: Boolean) {
        table.fillsViewportHeight = fill
    }

    val columnModel: TableColumnModel
        get() = table.columnModel

    val isEditing: Boolean
        get() = table.isEditing

    val cellEditor: TableCellEditor
        get() = table.cellEditor

    val editingRow: Int
        get() = table.editingRow

    fun convertRowIndexToModel(viewRowIndex: Int) = table.convertRowIndexToModel(viewRowIndex)

    fun setTableFont(font: Font)
    {
        this.font = font
        table.font = font
    }

    /**
     * Helpers
     */
    fun sortBy(columnIndex: Int, desc: Boolean)
    {
        rowSorter?.let {
            it.toggleSortOrder(columnIndex)
            if (desc) it.toggleSortOrder(columnIndex)
        }
    }

    fun setTableRenderer(renderer: TableCellRenderer)
    {
        table.setDefaultRenderer(Object::class.java, renderer)
    }

    fun getBuiltInRenderer(): TableCellRenderer = table.getDefaultRenderer(Object::class.java)

    fun setRenderer(columnIndex: Int, renderer: TableCellRenderer?)
    {
        val column = getColumn(columnIndex)
        column.cellRenderer = renderer
        val footerColumn = tableFooter.columnModel.getColumn(columnIndex)
        footerColumn.cellRenderer = renderer
    }

    fun <T> setComparator(columnIndex: Int, comp: Comparator<T>)
    {
        rowSorter?.setComparator(columnIndex, comp)
    }

    fun setColumnWidths(colStr: String)
    {
        val columnWidths = colStr.split(";")
        for (i in columnWidths.indices)
        {
            val colWidthStr = columnWidths[i]
            val colWidth = getColWidthForString(colWidthStr)

            if (colWidth == -1) continue

            setWidthForColumn(getColumn(i), colWidth)
            setWidthForColumn(tableFooter.columnModel.getColumn(i), colWidth)
        }
    }
    private fun setWidthForColumn(column: TableColumn, colWidth: Int)
    {
        column.preferredWidth = colWidth
        column.maxWidth = colWidth
    }
    private fun getColWidthForString(colWidthStr: String) = when(colWidthStr) {
        COL_WIDTH_STRING_DT -> COL_WIDTH_DT
        else -> colWidthStr.toInt()
    }

    fun disableSorting()
    {
        table.rowSorter = null
        sortingEnabled = false
    }

    fun removeColumn(colIx: Int) {
        val col = getColumn(colIx)
        table.removeColumn(col)
    }

    fun getColumn(col: Int): TableColumn = table.columnModel.getColumn(col)

    val selectedModelRow: Int
        get() {
            if (footerRowSelected()) {
                return TABLE_ROW_FOOTER
            }
            val viewRow = table.selectedRow
            return if (viewRow == -1) {
                -1
            } else table.convertRowIndexToModel(viewRow)
        }

    private fun footerRowSelected() = tableFooter.selectedRow > -1

    val selectedModelRows: IntArray
        get() {
            val viewRows = table.selectedRows
            val rowCount = viewRows.size
            val modelRows = IntArray(rowCount)
            for (i in 0 until rowCount) {
                val viewRow = viewRows[i]
                modelRows[i] = table.convertRowIndexToModel(viewRow)
            }
            return modelRows
        }

    val selectedViewRow: Int
        get() = if (footerRowSelected()) {
            TABLE_ROW_FOOTER
        } else table.selectedRow

    fun selectFirstRow() {
        selectRow(0)
    }

    fun selectRow(row: Int) {
        if (table.rowCount == 0) { //Nothing to select
            return
        }

        when (row)
        {
            -1 -> table.clearSelection()
            TABLE_ROW_FOOTER -> tableFooter.setRowSelectionInterval(0, 0)
            else -> {
                table.setRowSelectionInterval(row, row)
                table.scrollRectToVisible(Rectangle(table.getCellRect(row, 0, true)))
            }
        }
    }

    fun addFooterRow(row: Array<*>) {
        tableFooter.isVisible = true
        val model = tableFooter.model as DefaultTableModel
        if (model.rowCount > 0) {
            model.removeRow(0)
        }
        model.addRow(row)
    }

    fun removeAllRows() {
        model.rowCount = 0
        refreshRowCount()
    }

    fun scrollToBottom() {
        SwingUtilities.invokeLater {
            scrollPane.scrollToBottom()
        }
    }

    /**
     * Default methods
     */
    open fun isEditable(row: Int, col: Int) = false

    /**
     * TableColumnModelListener
     */
    override fun columnAdded(e: TableColumnModelEvent) {}

    override fun columnMarginChanged(e: ChangeEvent) {
        if (!tableFooter.isVisible) {
            return
        }
        for (i in 0 until columnCount) {
            val column = getColumn(i)
            val width = column.width
            val footerColumn = tableFooter.columnModel.getColumn(i)
            footerColumn.minWidth = width
            footerColumn.maxWidth = width
        }
        tableFooter.repaint()
    }

    override fun columnMoved(e: TableColumnModelEvent) {}
    override fun columnRemoved(e: TableColumnModelEvent) {}
    override fun columnSelectionChanged(e: ListSelectionEvent) {}

    /**
     * ListSelectionListener
     */
    override fun valueChanged(e: ListSelectionEvent)
    {
        when (e.source)
        {
            table.selectionModel -> updateSelection(table, tableFooter)
            else -> updateSelection(tableFooter, table)
        }

        listeners.forEach { it.selectionChanged(this) }
    }

    private fun updateSelection(src: JTable, dest: JTable)
    {
        val srcRow = src.selectedRow
        if (srcRow > -1) { //We've selected something in the source, so need to clear the destination table's selection
            dest.clearSelection()
        }
    }

    fun addKeyAction(key: Int, fn: () -> Unit)
    {
        val fullFn = fun () {
            if (selectedModelRows.isEmpty()) return
            fn()
        }

        table.addKeyAction(key, fullFn)
    }

    fun addRowSelectionListener(listener: RowSelectionListener) {
        listeners.add(listener)
    }

    fun addDoubleClickListener(listener: IDoubleClickListener) {
        clickListeners.add(listener)
    }

    fun setTableBorder(border: AbstractBorder)
    {
        scrollPane.border = border
        table.border = border
    }

    fun setTableBackground(bg: Color?) {
        table.background = bg
    }

    fun setHeaderFont(font: Font?) {
        table.tableHeader.font = font
    }

    override fun mouseClicked(e: MouseEvent) {
        if (e.clickCount == 2) {
            for (listener in clickListeners) {
                listener.doubleClicked(this)
            }
        }
    }

    companion object
    {
        const val TABLE_ROW_FOOTER = -2
        private const val COL_WIDTH_STRING_DT = "DT"
        private const val COL_WIDTH_DT = 115
    }
}