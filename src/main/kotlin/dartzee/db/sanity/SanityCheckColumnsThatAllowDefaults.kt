package dartzee.db.sanity

import dartzee.core.util.TableUtil

class SanityCheckColumnsThatAllowDefaults: ISanityCheck
{
    override fun runCheck(): List<AbstractSanityCheckResult>
    {
        val model = TableUtil.DefaultModel()
        model.addColumn("TableName")
        model.addColumn("ColumnName")

        val results = getColumnsAllowingDefaults().map { arrayOf(it.table, it.column) }
        results.forEach { model.addRow(it) }

        if (model.rowCount > 0)
        {
            return listOf(SanityCheckResultSimpleTableModel(model, "Columns that allow defaults"))
        }

        return listOf()
    }
}
