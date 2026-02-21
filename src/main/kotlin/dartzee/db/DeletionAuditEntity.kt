package dartzee.db

import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

/** Represents a row that's been deleted. Used by the sync to ensure deleted rows stay deleted */
class DeletionAuditEntity(database: Database = mainDatabase) :
    AbstractEntity<DeletionAuditEntity>(database) {
    /** DB fields */
    var entityName: EntityName = EntityName.DeletionAudit
    var entityId = ""

    override fun getTableName() = EntityName.DeletionAudit

    override fun includeInSync() = false

    override fun getCreateTableSqlSpecific() =
        "EntityName VARCHAR(255) NOT NULL, " + "EntityId VARCHAR(36) NOT NULL"

    override fun mergeImpl(otherDatabase: Database) {
        otherDatabase.executeUpdate("DELETE FROM $entityName WHERE RowId = '$entityId'")
    }

    companion object {
        fun factory(entity: AbstractEntity<*>, database: Database = mainDatabase) =
            factory(entity.getTableName(), entity.rowId, database)

        fun factory(
            entityName: EntityName,
            entityId: String,
            database: Database,
        ): DeletionAuditEntity {
            val result = DeletionAuditEntity(database)
            result.assignRowId()
            result.entityName = entityName
            result.entityId = entityId
            return result
        }

        fun factoryAndSave(entity: AbstractEntity<*>, database: Database = mainDatabase) =
            factory(entity, database).also { it.saveToDatabase() }
    }
}
