package dartzee.db.sanity

import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.DatabaseUtil
import dartzee.core.util.TableUtil

class SanityCheckUnexpectedTables: AbstractSanityCheck()
{
    override fun runCheck(): List<AbstractSanityCheckResult>
    {
        val entities = DartsDatabaseUtil.getAllEntitiesIncludingVersion()
        val tableNameSql = entities.joinToString{ "'${it.getTableNameUpperCase()}'"}

        val tm = TableUtil.DefaultModel()
        tm.addColumn("Schema")
        tm.addColumn("TableName")
        tm.addColumn("TableId")

        val sb = StringBuilder()
        sb.append(" SELECT s.SchemaName, t.TableName, t.TableId")
        sb.append(" FROM sys.systables t, sys.sysschemas s")
        sb.append(" WHERE t.SchemaId = s.SchemaId")
        sb.append(" AND t.TableType = 'T'")
        sb.append(" AND t.TableName NOT IN ($tableNameSql)")

        DatabaseUtil.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val schema = rs.getString("SchemaName")
                val tableName = rs.getString("TableName")
                val tableId = rs.getString("TableId")

                val row = arrayOf(schema, tableName, tableId)
                tm.addRow(row)
            }
        }

        if (tm.rowCount > 0)
        {
            return listOf(SanityCheckResultUnexpectedTables(tm))
        }

        return listOf()
    }
}