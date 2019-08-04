package burlton.dartzee.code.db.sanity

import burlton.dartzee.code.db.AbstractEntity

class SanityCheckResultUnsetColumns(private val columnName: String, entities: List<AbstractEntity<*>>) : AbstractSanityCheckResultEntities(entities)
{
    override fun getDescription(): String
    {
        return "$entityName rows where $columnName is unset"
    }
}
