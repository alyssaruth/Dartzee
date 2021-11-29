package dartzee.db.sanity

import dartzee.db.AbstractEntity
import dartzee.db.TableName
import dartzee.utils.DartsDatabaseUtil

class SanityCheckDanglingIdFields(val entity: AbstractEntity<*>): AbstractSanityCheck()
{
    private val sanityErrors = mutableListOf<AbstractSanityCheckResult>()

    override fun runCheck(): List<AbstractSanityCheckResult>
    {
        val idColumns = getIdColumns(entity)

        idColumns.forEach{
            checkForHangingValues(entity, it)
        }

        if (entity.getColumns().contains("EntityId"))
        {
            DartsDatabaseUtil.getAllEntities().forEach {
                checkForHangingEntityId(entity, it.getTableName())
            }
        }

        return sanityErrors
    }

    private fun checkForHangingValues(entity: AbstractEntity<*>, idColumn: String)
    {
        val tableStr = idColumn.substring(0, idColumn.length - 2)
        val referencedTable = TableName.valueOf(tableStr)

        val sb = StringBuilder()
        sb.append("$idColumn <> ''")
        sb.append(" AND NOT EXISTS (")
        sb.append(" SELECT 1 FROM $referencedTable ref")
        sb.append(" WHERE e.$idColumn = ref.RowId)")

        val whereSql = sb.toString()
        val entities = entity.retrieveEntities(whereSql, "e")

        val count = entities.size
        if (count > 0)
        {
            sanityErrors.add(SanityCheckResultDanglingIdFields(idColumn, referencedTable, entities))
        }
    }

    private fun checkForHangingEntityId(entity: AbstractEntity<*>, referencedTable: TableName)
    {
        val sb = StringBuilder()
        sb.append("EntityId <> ''")
        sb.append(" AND e.EntityName = '$referencedTable'")
        sb.append(" AND NOT EXISTS (")
        sb.append(" SELECT 1 FROM $referencedTable ref")
        sb.append(" WHERE e.EntityId = ref.RowId)")

        val whereSql = sb.toString()
        val entities = entity.retrieveEntities(whereSql, "e")

        val count = entities.size
        if (count > 0)
        {
            sanityErrors.add(SanityCheckResultDanglingIdFields("EntityId", referencedTable, entities))
        }
    }
}