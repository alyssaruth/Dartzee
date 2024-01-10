package dartzee.db

import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

class VersionEntity(database: Database = mainDatabase) : AbstractEntity<VersionEntity>(database) {
    var version = DartsDatabaseUtil.DATABASE_VERSION

    override fun getTableName() = EntityName.Version

    override fun getCreateTableSqlSpecific() = "Version INT NOT NULL"
}
