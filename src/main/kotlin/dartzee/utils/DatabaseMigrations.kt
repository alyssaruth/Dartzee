package dartzee.utils

import dartzee.core.util.getAttributeInt
import dartzee.core.util.jsonMapper
import dartzee.core.util.toXmlDoc
import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.db.DartsMatchEntity
import dartzee.db.DartzeeRuleEntity
import dartzee.db.DeletionAuditEntity

object DatabaseMigrations
{
    fun getConversionsMap(): Map<Int, List<(database: Database) -> Any>>
    {
        return mapOf(
            16 to listOf { db -> convertMatchParams(db) },
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

    /**
     * V16 -> V17
     */
    fun convertMatchParams(database: Database)
    {
        val matches = DartsMatchEntity(database).retrieveEntities("MatchParams <> ''")
        matches.forEach { match ->
            val params = match.matchParams
            val map = readMatchParamXml(params)
            match.matchParams = jsonMapper().writeValueAsString(map)
            match.saveToDatabase()
        }
    }
    private fun readMatchParamXml(matchParams: String): Map<Int, Int>
    {
        val map = mutableMapOf<Int, Int>()
        val doc = matchParams.toXmlDoc() ?: return map
        val root = doc.documentElement

        map[1] = root.getAttributeInt("First")
        map[2] = root.getAttributeInt("Second")
        map[3] = root.getAttributeInt("Third")
        map[4] = root.getAttributeInt("Fourth")
        map[5] = root.getAttributeInt("Fifth")
        map[6] = root.getAttributeInt("Sixth")

        return map
    }

    fun runScript(database: Database, version: Int, scriptName: String): Boolean
    {
        val resourcePath = "/sql/v$version/"
        val rsrc = javaClass.getResource("$resourcePath$scriptName").readText()

        val batches = rsrc.split(";")

        return database.executeUpdates(batches)
    }
}
