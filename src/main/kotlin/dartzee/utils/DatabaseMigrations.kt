package dartzee.utils

import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.db.DartzeeRuleEntity
import dartzee.db.DeletionAuditEntity

object DatabaseMigrations
{
    fun getConversionsMap(): Map<Int, List<(database: Database) -> Any>>
    {
        return mapOf(
            17 to listOf(
                { db -> runScript(db, 18, "1. DartzeeRule.sql") },
                { db -> convertDartzeeCalculationResults(db) }
            ),
            18 to listOf { db -> createDeletionAudit(db) }
        )
    }

    /**
     * V18 -> V19
     */
    fun createDeletionAudit(database: Database)
    {
        DeletionAuditEntity(database).createTable()
    }

    /**
     * V17 -> V18
     */
    fun convertDartzeeCalculationResults(database: Database)
    {
        val rules = DartzeeRuleEntity(database).retrieveEntities()
        rules.forEach { ruleEntity ->
            val calculationResult = DartzeeRuleCalculationResult.fromDbStringOLD(ruleEntity.calculationResult)
            ruleEntity.calculationResult = calculationResult.toDbString()
            ruleEntity.saveToDatabase()
        }
    }

    fun runScript(database: Database, version: Int, scriptName: String): Boolean
    {
        val resourcePath = "/sql/v$version/"
        val rsrc = javaClass.getResource("$resourcePath$scriptName").readText()

        val batches = rsrc.split(";")

        return database.executeUpdates(batches)
    }
}
