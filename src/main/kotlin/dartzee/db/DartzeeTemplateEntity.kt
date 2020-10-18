package dartzee.db

import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

const val DARTZEE_TEMPLATE = "DartzeeTemplate"

class DartzeeTemplateEntity(database: Database = mainDatabase): AbstractEntity<DartzeeTemplateEntity>(database)
{
    var name = ""

    override fun getTableName() = DARTZEE_TEMPLATE

    override fun getCreateTableSqlSpecific(): String
    {
        return ("Name VARCHAR(1000) NOT NULL")
    }

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
