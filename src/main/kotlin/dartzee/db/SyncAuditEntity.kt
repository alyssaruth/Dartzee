package dartzee.db

import dartzee.`object`.DartsClient
import dartzee.main.getDeviceId
import dartzee.main.getUsername
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.Database
import dartzee.utils.InjectedThings
import java.sql.Timestamp

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

    companion object
    {
        fun insertSyncAudit(database: Database, remoteName: String)
        {
            val entity = SyncAuditEntity(database)
            entity.assignRowId()
            entity.appVersion = DARTS_VERSION_NUMBER
            entity.deviceId = getDeviceId()
            entity.username = getUsername()
            entity.operatingSystem = DartsClient.operatingSystem
            entity.remoteName = remoteName

            entity.saveToDatabase()
        }

        fun getLastSyncDate(database: Database, remoteName: String): Timestamp?
        {
            val dao = SyncAuditEntity(database)
            val entities = dao.retrieveEntities("RemoteName = '$remoteName'")
            val latest = entities.maxBy { it.dtLastUpdate }
            return latest?.dtLastUpdate
        }
    }

}
