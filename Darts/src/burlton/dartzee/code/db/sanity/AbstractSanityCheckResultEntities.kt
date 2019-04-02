package burlton.dartzee.code.db.sanity

import burlton.dartzee.code.bean.TableModelEntity
import burlton.dartzee.code.db.AbstractEntity
import burlton.dartzee.code.utils.DatabaseUtil.Companion.deleteRowsFromTable
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.util.DialogUtil
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JOptionPane

abstract class AbstractSanityCheckResultEntities(val entities: List<AbstractEntity<*>>): AbstractSanityCheckResult()
{
    val entityName = entities.first().getTableName()

    override fun getResultsModel() = TableModelEntity(entities)
    override fun getDeleteAction(t: ScrollTable): Action?
    {
        return object : AbstractAction()
        {
            override fun actionPerformed(e: ActionEvent)
            {
                val rows = t.selectedModelRows
                if (rows.isEmpty())
                {
                    return
                }

                val ans = DialogUtil.showQuestion("Are you sure you want to delete ${rows.size} row(s) from $entityName?", false)
                if (ans == JOptionPane.YES_OPTION)
                {
                    val success = deleteSelectedRows(t, rows)
                    if (!success)
                    {
                        DialogUtil.showError("An error occurred deleting the rows. You should re-run the sanity check and check logs.")
                    }
                }
            }
        }
    }

    private fun deleteSelectedRows(t: ScrollTable, selectedRows: IntArray): Boolean
    {
        val rowIds = mutableListOf<String>()
        for (i in selectedRows.indices)
        {
            val rowId = t.getValueAt(selectedRows[i], 0) as String
            rowIds.add(rowId)
        }

        return deleteRowsFromTable(entityName, rowIds)
    }

    override fun getCount() = entities.size
}
