package dartzee.db.sanity

import dartzee.db.AbstractEntity
import dartzee.utils.DartsDatabaseUtil

fun getIdColumns(entity: AbstractEntity<*>): List<String>
{
    val potentialIdColumns = DartsDatabaseUtil.getAllEntities().map { "${it.getTableName()}Id" }
    return entity.getColumns().filter{ potentialIdColumns.contains(it) }
}