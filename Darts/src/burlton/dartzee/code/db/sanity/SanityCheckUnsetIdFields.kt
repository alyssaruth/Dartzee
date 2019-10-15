package burlton.dartzee.code.db.sanity

import burlton.dartzee.code.db.AbstractEntity
import burlton.dartzee.code.utils.DartsDatabaseUtil

class SanityCheckUnsetIdFields(val entity: AbstractEntity<*>): AbstractSanityCheck()
{
    private val sanityErrors = mutableListOf<AbstractSanityCheckResult>()

    override fun runCheck(): List<AbstractSanityCheckResult>
    {
        val columns = entity.getColumns()
        val potentialIdColumns = DartsDatabaseUtil.getAllEntities().map { "${it.getTableName()}Id" }
        val idColumns = columns.filter{ potentialIdColumns.contains(it) }

        idColumns.forEach{
            if (!entity.columnCanBeUnset(it))
            {
                checkForUnsetValues(entity, it)
            }
        }

        return sanityErrors
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