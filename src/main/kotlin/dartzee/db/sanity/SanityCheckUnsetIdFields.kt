package dartzee.db.sanity

import dartzee.db.AbstractEntity

class SanityCheckUnsetIdFields(val entity: AbstractEntity<*>): ISanityCheck
{
    private val sanityErrors = mutableListOf<AbstractSanityCheckResult>()

    override fun runCheck(): List<AbstractSanityCheckResult>
    {
        val idColumns = getIdColumns(entity)

        idColumns.forEach { column ->
            if (!entity.columnCanBeUnset(column))
            {
                checkForUnsetValues(entity, column)
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