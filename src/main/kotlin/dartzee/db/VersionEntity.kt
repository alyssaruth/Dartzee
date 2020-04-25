package dartzee.db

import dartzee.utils.DartsDatabaseUtil

class VersionEntity : AbstractEntity<VersionEntity>()
{
    var version = DartsDatabaseUtil.DATABASE_VERSION

    override fun getTableName() = "Version"

    override fun getCreateTableSqlSpecific() = "Version INT NOT NULL"

    companion object
    {
        fun retrieveCurrentDatabaseVersion() = VersionEntity().retrieveEntity("1 = 1")

        fun insertVersion()
        {
            val versionEntity = VersionEntity()
            versionEntity.assignRowId()
            versionEntity.version = DartsDatabaseUtil.DATABASE_VERSION
            versionEntity.saveToDatabase()
        }
    }
}
