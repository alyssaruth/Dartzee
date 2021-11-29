package dartzee.db

import dartzee.utils.Database

class LocalIdGenerator(private val database: Database)
{
    private val uniqueIdSyncObject = Any()
    val hmLastAssignedIdByTableName = mutableMapOf<TableName, Long>()

    fun generateLocalId(tableName: TableName): Long
    {
        synchronized(uniqueIdSyncObject)
        {
            val lastAssignedId = hmLastAssignedIdByTableName[tableName] ?: retrieveLastAssignedId(tableName)

            val nextId = lastAssignedId + 1
            hmLastAssignedIdByTableName[tableName] = nextId

            return nextId
        }
    }
    private fun retrieveLastAssignedId(tableName: TableName): Long
    {
        return database.executeQueryAggregate("SELECT MAX(LocalId) FROM $tableName").toLong()
    }
}