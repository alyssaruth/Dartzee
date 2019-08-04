package burlton.dartzee.code.db.sanity

import burlton.dartzee.code.db.AbstractEntity

class SanityCheckUnsetOrHangingFields(val entity: AbstractEntity<*>): AbstractSanityCheck()
{
    private val sanityErrors = mutableListOf<AbstractSanityCheckResult>()

    override fun runCheck(): List<AbstractSanityCheckResult>
    {
        val columns = entity.getColumns()
        val idColumns = columns.filter{ it.endsWith("Id") && it != "RowId" && it != "LocalId" }

        idColumns.forEach{
            checkForHangingValues(entity, it)
            if (!entity.columnCanBeUnset(it))
            {
                checkForUnsetValues(entity, it)
            }
        }

        return sanityErrors
    }

    private fun checkForHangingValues(entity: AbstractEntity<*>, idColumn: String)
    {
        val referencedTable = idColumn.substring(0, idColumn.length - 2)

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
            sanityErrors.add(SanityCheckResultHangingEntities(idColumn, entities))
        }
    }

    private fun checkForUnsetValues(entity: AbstractEntity<*>, idColumn: String)
    {
        val whereSql = "$idColumn = ''"
        val entities = entity.retrieveEntities(whereSql)

        val count = entities.size
        if (count > 0)
        {
            sanityErrors.add(SanityCheckResultUnsetColumns(idColumn, entities))
        }
    }


}