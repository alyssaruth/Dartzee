package burlton.dartzee.code.db.sanity

import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.TableUtil.DefaultModel
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JOptionPane

class SanityCheckResultUnexpectedTables(model: DefaultModel) : SanityCheckResultSimpleTableModel(model, "Unexpected Tables")
{
    override fun getDeleteAction(t: ScrollTable) =
        object : AbstractAction()
        {
            override fun actionPerformed(e: ActionEvent)
            {
                val rows = t.selectedModelRows
                if (rows.isEmpty())
                {
                    return
                }

                val tableNames = rows.map{ t.getValueAt(it, 1) as String }

                val tableList = tableNames.joinToString("\n")
                val ans = DialogUtil.showQuestion("Are you sure you want to drop the following tables from the database?\n\n$tableList", false)
                if (ans == JOptionPane.YES_OPTION)
                {
                    val success = deleteSelectedTables(tableNames)
                    if (!success)
                    {
                        DialogUtil.showError("An error occurred dropping the tables. You should re-run the sanity check and check logs.")
                    }
                }
            }
        }


    private fun deleteSelectedTables(tableNames: List<String>): Boolean
    {
        var success = true
        for (tableName in tableNames)
        {
            success = DatabaseUtil.dropTable(tableName) && success
        }

        return success
    }

    override fun autoFix()
    {
        val tableNames = getTableNames()

        val response = DialogUtil.showQuestion("This will drop all ${tableNames.size} tables from the database. Are you sure?", false)
        if (response == JOptionPane.NO_OPTION)
        {
            return
        }

        val success = deleteSelectedTables(tableNames)
        if (!success)
        {
            DialogUtil.showError("An error occurred dropping the tables. You should re-run the sanity check and check logs.")
        }
    }

    private fun getTableNames(): List<String>
    {
        val model = getResultsModel()

        val rowCount = model.rowCount

        val tableNames = mutableListOf<String>()
        for (i in 0 until rowCount)
        {
            val tableName = model.getValueAt(i, 1) as String
            tableNames.add(tableName)
        }

        return tableNames
    }
}
