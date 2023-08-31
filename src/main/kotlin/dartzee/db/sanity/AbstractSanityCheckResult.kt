package dartzee.db.sanity

import dartzee.core.bean.ScrollTable
import dartzee.core.screen.TableModelDialog
import dartzee.core.util.DialogUtil
import java.awt.event.KeyEvent
import javax.swing.table.DefaultTableModel

abstract class AbstractSanityCheckResult
{
    abstract fun getResultsModel(): DefaultTableModel
    abstract fun getDescription(): String
    abstract fun getCount(): Int

    fun getResultsDialog(): TableModelDialog
    {
        val t = getScrollTable()
        t.model = getResultsModel()

        val deleteAction = getDeleteAction(t)

        if (deleteAction != null)
        {
            t.addKeyAction(KeyEvent.VK_DELETE, deleteAction)
        }

        return TableModelDialog(getDescription(), t)
    }

    open fun getDeleteAction(t: ScrollTable): (() -> Unit)? = null

    open fun getScrollTable() = ScrollTable()

    open fun autoFix()
    {
        DialogUtil.showErrorOLD("No auto-fix available.")
    }

    override fun toString() = getDescription()
}
