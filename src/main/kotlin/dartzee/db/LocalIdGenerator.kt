package dartzee.db

import dartzee.utils.Database

class LocalIdGenerator(private val database: Database)
{
    private val uniqueIdSyncObject = Any()
    val hmLastAssignedIdByTableName = mutableMapOf<String, Long>()

    fun generateLocalId(tableName: String): Long
    {
        synchronized(uniqueIdSyncObject)
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