package burlton.desktopcore.code.bean

import burlton.core.code.util.InjectedCore
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class ScrollTableOrdered : ScrollTable(), ActionListener
{
    private val panelOrdering = JPanel()
    val btnMoveUp = JButton("")
    val btnMoveDown = JButton("")
    val btnRandomize = JButton("")

    init
    {
        add(panelOrdering, BorderLayout.EAST)
        panelOrdering.layout = MigLayout("al center center, wrap, gapy 20")
        btnMoveUp.icon = ImageIcon(javaClass.getResource("/buttons/upArrow.png"))
        btnMoveUp.preferredSize = Dimension(40, 40)
        btnMoveUp.toolTipText = "Move row up"

        panelOrdering.add(btnMoveUp, "cell 0 3")
        btnMoveDown.icon = ImageIcon(javaClass.getResource("/buttons/downArrow.png"))
        btnMoveDown.preferredSize = Dimension(40, 40)
        btnMoveDown.toolTipText = "Move row down"

        panelOrdering.add(btnMoveDown, "cell 0 4")
        btnRandomize.icon = ImageIcon(javaClass.getResource("/buttons/dice.png"))
        btnRandomize.preferredSize = Dimension(40, 40)
        btnRandomize.toolTipText = "Randomise row order"

        panelOrdering.add(btnRandomize, "cell 0 5")

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        disableSorting()

        btnMoveUp.addActionListener(this)
        btnMoveDown.addActionListener(this)
        btnRandomize.addActionListener(this)
    }

    fun addButtonToOrderingPanel(btn: JButton, row: Int)
    {
        btn.preferredSize = Dimension(40, 40)
        panelOrdering.add(btn, "cell 0 $row")
    }

    /**
     * ActionListener
     */
    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnMoveUp -> moveSelectedRowUp()
            btnMoveDown -> moveSelectedRowDown()
            btnRandomize -> scrambleOrder()
        }
    }

    private fun moveSelectedRowUp()
    {
        val row = selectedModelRow
        if (row <= 0)
        {
            //Nothing to do
            return
        }

        model.moveRow(row, row, row - 1)
        selectRow(row - 1)
    }

    private fun moveSelectedRowDown()
    {
        val row = selectedModelRow
        if (row == rowCount - 1 || row == -1)
        {
            //Nothing to do
            return
        }

        model.moveRow(row, row, row + 1)
        selectRow(row + 1)
    }

    private fun scrambleOrder()
    {
        val allRows = getAllRows()
        InjectedCore.collectionShuffler.shuffleCollection(allRows)

        setNewOrder(allRows)
    }

    inline fun <R : Comparable<R>> reorderRows(crossinline selector: (Array<Any?>) -> R?)
    {
        val rows = getAllRows()
        rows.sortBy(selector)

        setNewOrder(rows)
    }

    fun setNewOrder(orderedRows: List<Array<Any?>>)
    {
        removeAllRows()

        orderedRows.forEach { addRow(it) }
    }

    fun getAllRows(): MutableList<Array<Any?>>
    {
        val rows = mutableListOf<Array<Any?>>()
        for (i in 0 until rowCount)
        {
            val row = getRow(i)
            rows.add(row)
        }

        return rows
    }

    private fun getRow(rowIx: Int): Array<Any?>
    {
        val columnCount = columnCount
        val row = arrayOfNulls<Any>(columnCount)
        for (i in 0 until getColumnCount()) {
            row[i] = getValueAt(rowIx, i)
        }

        return row
    }
}