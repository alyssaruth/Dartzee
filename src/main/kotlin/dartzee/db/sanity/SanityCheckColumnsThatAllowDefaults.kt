package dartzee.db.sanity

import dartzee.core.util.TableUtil
import dartzee.utils.InjectedThings.mainDatabase

class SanityCheckColumnsThatAllowDefaults: AbstractSanityCheck()
{
    override fun runCheck(): List<AbstractSanityCheckResult>
    {
        val sb = StringBuilder()
        sb.append("SELECT t.TableName, c.ColumnName ")
        sb.append("FROM sys.systables t, sys.syscolumns c ")
        sb.append("WHERE c.ReferenceId = t.TableId ")
        sb.append("AND t.TableType = 'T' ")
        sb.append("AND c.ColumnDefault IS NOT NULL")

        val model = TableUtil.DefaultModel()
        model.addColumn("TableName")
        model.addColumn("ColumnName")

        mainDatabase.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val tableName = rs.getString("TableName")
                val columnName = rs.getString("ColumnName")

                val row = arrayOf(tableName, columnName)
                model.addRow(row)
            }
        }

        if (model.rowCount > 0)
        {
            return listOf(SanityCheckResultSimpleTableModel(model, "Columns that allow defaults"))
        }

        return listOf()
    }
}
