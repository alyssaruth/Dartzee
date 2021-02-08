package dartzee.utils

import dartzee.achievements.*
import dartzee.core.util.getAttributeInt
import dartzee.core.util.jsonMapper
import dartzee.core.util.toXmlDoc
import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.db.DartsMatchEntity
import dartzee.db.DartzeeRuleEntity
import dartzee.db.SyncAuditEntity

object DatabaseMigrations
{
    fun getConversionsMap(): Map<Int, List<(database: Database) -> Any>>
    {
        return mapOf(
            15 to listOf (
                { db -> SyncAuditEntity(db).createTable() },
                { db -> runScript(db, 16, "1. Achievement.sql") },
                { db -> convertAchievement(AchievementType.X01_GAMES_WON, db) },
                { db -> convertAchievement(AchievementType.GOLF_GAMES_WON, db) },
                { db -> convertAchievement(AchievementType.CLOCK_GAMES_WON, db) },
                { db -> convertAchievement(AchievementType.DARTZEE_GAMES_WON, db) },
                { db -> convertAchievement(AchievementType.CLOCK_BRUCEY_BONUSES, db) },
                { db -> convertAchievement(AchievementType.GOLF_POINTS_RISKED, db) }
            ),
            16 to listOf (
                { db -> convertMatchParams(db) }
            ),
            17 to listOf(
                { db -> convertDartzeeCalculationResults(db) }
            )
        )
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

    /**
     * V14 -> V15
     */
    private fun convertAchievement(type: AchievementType, database: Database)
    {
        try
        {
            getAchievementForType(type)!!.runConversion(emptyList(), database)
        }
        finally
        {
            database.dropUnexpectedTables()
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
