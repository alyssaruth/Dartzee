package dartzee.db.sanity

import dartzee.core.bean.ScrollTable
import dartzee.core.screen.TableModelDialog
import dartzee.core.util.DialogUtil
import java.awt.event.KeyEvent
import javax.swing.table.DefaultTableModel

open class SanityCheckResult(val resultsModel: DefaultTableModel, val description: String) {
    fun getCount() = resultsModel.rowCount

    fun getResultsDialog(): TableModelDialog {
        val t = getScrollTable()
        t.model = resultsModel

        val deleteAction = getDeleteAction(t)

        if (deleteAction != null) {
            t.addKeyAction(KeyEvent.VK_DELETE, deleteAction)
        }

        return TableModelDialog(description, t)
    }

    open fun getDeleteAction(t: ScrollTable): (() -> Unit)? = null

    open fun getScrollTable() = ScrollTable()

    open fun autoFix() {
        DialogUtil.showError("No auto-fix available.")
    }

    override fun toString() = description
}
