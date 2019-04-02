package burlton.dartzee.code.db.sanity

import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.screen.TableModelDialog
import burlton.desktopcore.code.util.DialogUtil
import java.awt.event.KeyEvent
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.KeyStroke
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
            t.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete")
            t.actionMap.put("Delete", deleteAction)
        }

        return TableModelDialog(getDescription(), t)
    }

    open fun getDeleteAction(t: ScrollTable): Action?
    {
        return null
    }

    open fun getScrollTable() = ScrollTable()

    open fun autoFix()
    {
        DialogUtil.showError("No auto-fix available.")
    }

    override fun toString() = getDescription()
}
