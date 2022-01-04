package dartzee.db

import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

/**
 * Represents a row that's been deleted.
 * Used by the sync to ensure deleted rows stay deleted
 */
class DeletionAuditEntity(database: Database = mainDatabase): AbstractEntity<DeletionAuditEntity>(database)
{
    /**
     * DB fields
     */
    var entityName: EntityName = EntityName.DeletionAudit
    var entityId = ""

    override fun getTableName() = EntityName.DeletionAudit

    override fun includeInSync() = false

    override fun getCreateTableSqlSpecific(): String
    {
        return ("EntityName VARCHAR(255) NOT NULL, "
                + "EntityId VARCHAR(36) NOT NULL")
    }

    override fun mergeImpl(otherDatabase: Database)
    {
        otherDatabase.executeUpdate("DELETE FROM $entityName WHERE RowId = '$entityId'")
    }

    companion object
    {
        fun factory(entityName: EntityName, entityId: String): DeletionAuditEntity
        {
            val entity = DeletionAuditEntity()
            entity.assignRowId()
            entity.entityName = entityName
            entity.entityId = entityId
            return entity
        }

        fun factoryAndSave(entityName: EntityName, entityId: String): DeletionAuditEntity
        {
            return factory(entityName, entityId).also { it.saveToDatabase() }
        }
    }
}