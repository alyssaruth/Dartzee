package dartzee.db.sanity

import dartzee.bean.TableModelEntity
import dartzee.core.bean.ScrollTable
import dartzee.core.util.DialogUtil
import dartzee.db.AbstractEntity
import dartzee.utils.InjectedThings.mainDatabase
import javax.swing.JOptionPane

abstract class AbstractSanityCheckResultEntities(val entities: List<AbstractEntity<*>>) :
    AbstractSanityCheckResult() {
    val entityName = entities.first().getTableName()

    override fun getResultsModel() = TableModelEntity(entities)

    override fun getDeleteAction(t: ScrollTable): (() -> Unit)? =
        fun() {
            val rows = t.selectedModelRows

            val ans =
                DialogUtil.showQuestionOLD(
                    "Are you sure you want to delete ${rows.size} row(s) from $entityName?",
                    false
                )
            if (ans == JOptionPane.YES_OPTION) {
                val success = deleteSelectedRows(t, rows)
                if (!success) {
                    DialogUtil.showErrorOLD(
                        "An error occurred deleting the rows. You should re-run the sanity check and check logs."
                    )
                }
            }
        }

    private fun deleteSelectedRows(t: ScrollTable, selectedRows: IntArray): Boolean {
        val rowIds = mutableListOf<String>()
        for (i in selectedRows.indices) {
            val rowId = t.getNonNullValueAt(selectedRows[i], 0) as String
            rowIds.add(rowId)
        }

        return mainDatabase.deleteRowsFromTable(entityName, rowIds)
    }

    override fun getCount() = entities.size
}
