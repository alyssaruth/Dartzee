package dartzee.db

import dartzee.utils.Database
import dartzee.utils.InjectedThings.database

object LocalIdGenerator
{
    private val UNIQUE_ID_SYNCH_OBJECT = Any()
    val hmLastAssignedIdByTableName = mutableMapOf<String, Long>()

    fun generateLocalId(database: Database, tableName: String): Long
    {
        synchronized(UNIQUE_ID_SYNCH_OBJECT)
        {
            val lastAssignedId = hmLastAssignedIdByTableName[tableName] ?: retrieveLastAssignedId(database, tableName)

            val nextId = lastAssignedId + 1
            hmLastAssignedIdByTableName[tableName] = nextId

            return nextId
        }
    }
    private fun retrieveLastAssignedId(database: Database, tableName: String): Long
    {
        return database.executeQueryAggregate("SELECT MAX(LocalId) FROM $tableName").toLong()
    }
}