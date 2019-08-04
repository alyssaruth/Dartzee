package burlton.desktopcore.code.screen

import burlton.desktopcore.code.bean.ScrollTable
import java.awt.BorderLayout

/**
 * Simple dialog to show a table
 */
class TableModelDialog(title: String, val table: ScrollTable) : SimpleDialog()
{
    init
    {
        setTitle(title)
        setSize(600, 400)
        isModal = true

        contentPane.add(table, BorderLayout.CENTER)
    }

    /**
     * Configure things about the table
     */
    fun setColumnWidths(colWidthsStr: String) = table.setColumnWidths(colWidthsStr)

    override fun okPressed() = dispose()
    override fun allowCancel() = false
}