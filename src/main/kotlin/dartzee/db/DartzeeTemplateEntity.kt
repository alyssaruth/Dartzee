package dartzee.db

import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

class DartzeeTemplateEntity(database: Database = mainDatabase): AbstractEntity<DartzeeTemplateEntity>(database)
{
    var name = ""

    override fun getTableName() = EntityName.DartzeeTemplate

    override fun getCreateTableSqlSpecific() = "Name VARCHAR(1000) NOT NULL"

    companion object
    {
        fun factoryAndSave(name: String): DartzeeTemplateEntity
        {
            val entity = DartzeeTemplateEntity()
            entity.assignRowId()
            entity.name = name
            entity.saveToDatabase()
            return entity
        }
    }

    override fun toString() = name
}
