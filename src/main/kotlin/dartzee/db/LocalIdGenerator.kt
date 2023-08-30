package dartzee.db

import dartzee.utils.Database

class LocalIdGenerator(private val database: Database)
{
    private val uniqueIdSyncObject = Any()
    private val hmLastAssignedIdByEntityName = mutableMapOf<EntityName, Long>()

    fun clearCache()
    {
        hmLastAssignedIdByEntityName.clear()
    }

    fun generateLocalId(entityName: EntityName): Long
    {
        synchronized(uniqueIdSyncObject)
        {
            val lastAssignedId = hmLastAssignedIdByEntityName[entityName] ?: retrieveLastAssignedId(entityName)

            val nextId = lastAssignedId + 1
            hmLastAssignedIdByEntityName[entityName] = nextId

            return nextId
        }
    }
    private fun retrieveLastAssignedId(entityName: EntityName) =
        database.executeQueryAggregate("SELECT MAX(LocalId) FROM $entityName").toLong()
}