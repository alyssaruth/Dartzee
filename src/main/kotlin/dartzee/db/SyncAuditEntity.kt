package dartzee.db

import dartzee.utils.Database
import dartzee.utils.InjectedThings
import java.sql.Timestamp

class SyncAuditEntity(database: Database = InjectedThings.mainDatabase) : AbstractEntity<SyncAuditEntity>(database)
{
    var remoteName = ""

    override fun getTableName() = "SyncAudit"

    override fun getCreateTableSqlSpecific() = "RemoteName VARCHAR(255) NOT NULL"

    fun getLastSyncDate(remoteName: String): Timestamp?
    {
        val entities = retrieveEntities("RemoteName = '$remoteName'")
        val latest = entities.maxBy { it.dtLastUpdate }
        return latest?.dtLastUpdate
    }
}
