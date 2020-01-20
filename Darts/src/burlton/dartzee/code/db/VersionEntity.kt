package burlton.dartzee.code.db

import burlton.desktopcore.code.util.Debug
import burlton.dartzee.code.utils.DartsDatabaseUtil

class VersionEntity : AbstractEntity<VersionEntity>()
{
    var version = DartsDatabaseUtil.DATABASE_VERSION

    override fun getTableName() = "Version"

    override fun getCreateTableSqlSpecific() = "Version INT NOT NULL"

    companion object
    {
        fun retrieveCurrentDatabaseVersion(): VersionEntity?
        {
            val entities = VersionEntity().retrieveEntities("1 = 1")
            return if (entities.isEmpty()) null
            else
            {
                if (entities.size > 1)
                {
                    Debug.stackTrace("Found ${entities.size} rows in Version - should only be 1")
                }

                entities.first()
            }
        }

        fun insertVersion()
        {
            val versionEntity = VersionEntity()
            versionEntity.assignRowId()
            versionEntity.version = DartsDatabaseUtil.DATABASE_VERSION
            versionEntity.saveToDatabase()
        }
    }
}
