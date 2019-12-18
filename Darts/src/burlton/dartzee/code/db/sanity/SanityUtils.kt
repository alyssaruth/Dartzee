package burlton.dartzee.code.db.sanity

import burlton.dartzee.code.db.AbstractEntity
import burlton.dartzee.code.utils.DartsDatabaseUtil

fun getIdColumns(entity: AbstractEntity<*>): List<String>
{
    val potentialIdColumns = DartsDatabaseUtil.getAllEntities().map { "${it.getTableName()}Id" }
    return entity.getColumns().filter{ potentialIdColumns.contains(it) }
}