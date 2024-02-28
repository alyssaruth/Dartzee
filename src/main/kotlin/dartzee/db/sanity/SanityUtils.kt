package dartzee.db.sanity

import dartzee.db.AbstractEntity
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings.mainDatabase

fun getIdColumns(entity: AbstractEntity<*>): List<String> {
    val potentialIdColumns = DartsDatabaseUtil.getAllEntities().map { "${it.getTableName()}Id" }
    return entity.getColumns().filter { potentialIdColumns.contains(it) }
}

data class TableAndColumn(val table: String, val column: String)

fun getColumnsAllowingDefaults(): List<TableAndColumn> {
    val sb = StringBuilder()
    sb.append("SELECT t.TableName, c.ColumnName ")
    sb.append("FROM sys.systables t, sys.syscolumns c ")
    sb.append("WHERE c.ReferenceId = t.TableId ")
    sb.append("AND t.TableType = 'T' ")
    sb.append("AND c.ColumnDefault IS NOT NULL")

    return mainDatabase.retrieveAsList(sb) { rs ->
        val tableName = rs.getString("TableName")
        val columnName = rs.getString("ColumnName")

        TableAndColumn(tableName, columnName)
    }
}
