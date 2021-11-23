package dartzee.db

import dartzee.`object`.DartsClient
import dartzee.main.getDeviceId
import dartzee.main.getUsername
import dartzee.sync.LastSyncData
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.Database
import dartzee.utils.InjectedThings

class SyncAuditEntity(database: Database = InjectedThings.mainDatabase) : AbstractEntity<SyncAuditEntity>(database)
{
    var remoteName = ""
    var username = ""
    var appVersion = ""
    var deviceId = ""
    var operatingSystem = ""

    override fun getTableName() = "SyncAudit"

    override fun getCreateTableSqlSpecific() =
        "RemoteName VARCHAR(255) NOT NULL, Username VARCHAR(1000) NOT NULL, AppVersion VARCHAR(255) NOT NULL, " +
        "DeviceId VARCHAR(36) NOT NULL, OperatingSystem VARCHAR(1000) NOT NULL"

    override fun includeInSync() = false

    companion object
    {
        fun insertSyncAudit(database: Database, remoteName: String): SyncAuditEntity
        {
            val entity = SyncAuditEntity(database)
            entity.assignRowId()
            entity.appVersion = DARTS_VERSION_NUMBER
            entity.deviceId = getDeviceId()
            entity.username = getUsername()
            entity.operatingSystem = DartsClient.operatingSystem
            entity.remoteName = remoteName

            entity.saveToDatabase()
            return entity
        }

        fun getLastSyncData(database: Database): LastSyncData?
        {
            val dao = SyncAuditEntity(database)
            val entities = dao.retrieveEntities()
            val latest = entities.maxByOrNull { it.dtLastUpdate } ?: return null
            return LastSyncData(latest.remoteName, latest.dtLastUpdate)
        }
    }

}
