package burlton.dartzee.code.db

class DartzeeTemplateEntity: AbstractEntity<DartzeeTemplateEntity>()
{
    var name = ""
    var ruleCount = -1
    var difficulty = -1.0

    override fun getTableName() = "DartzeeTemplate"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("Name VARCHAR(1000) NOT NULL, "
                + "RuleCount INT NOT NULL, "
                + "Difficulty DOUBLE PRECISION NOT NULL")
    }

    companion object
    {
        fun factoryAndSave(name: String, ruleCount: Int, difficulty: Double): DartzeeTemplateEntity
        {
            val entity = DartzeeTemplateEntity()
            entity.assignRowId()
            entity.name = name
            entity.ruleCount = ruleCount
            entity.difficulty = difficulty
            entity.saveToDatabase()
            return entity
        }
    }
}
