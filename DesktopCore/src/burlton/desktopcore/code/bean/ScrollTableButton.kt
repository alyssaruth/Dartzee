package burlton.desktopcore.code.bean

import javax.swing.Action
import javax.swing.table.DefaultTableModel

class ScrollTableButton(hmColumnToAction: Map<Int, Action>, tm: DefaultTableModel): ScrollTable()
{
    private var buttonColumns = hmColumnToAction.keys.toList()

    init
    {
        model = tm

        buttonColumns.forEach{ ButtonColumn(this, hmColumnToAction[it], it)}
    }

    override fun isEditable(row: Int, col: Int): Boolean
    {
        return buttonColumns.contains(col)
    }
}
