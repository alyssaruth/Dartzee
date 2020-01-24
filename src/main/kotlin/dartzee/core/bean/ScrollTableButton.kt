package dartzee.core.bean

import javax.swing.Action
import javax.swing.table.DefaultTableModel

class ScrollTableButton(tm: DefaultTableModel): ScrollTable()
{
    private var buttonColumns = mutableListOf<Int>()

    init
    {
        model = tm
    }

    override fun isEditable(row: Int, col: Int): Boolean
    {
        return buttonColumns.contains(col)
    }

    fun setButtonColumn(column: Int, action: Action)
    {
        buttonColumns.add(column)
        ButtonColumn(this, action, column)
    }
}
