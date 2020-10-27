package dartzee.utils

import dartzee.achievements.*
import dartzee.ai.DartsAiModel
import dartzee.ai.DartsAiModelOLD
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.db.SyncAuditEntity
import dartzee.game.ClockType
import dartzee.game.RoundTheClockConfig

object DatabaseMigrations
{
    fun getConversionsMap(): Map<Int, List<(database: Database) -> Any>>
    {
        return mapOf(
            13 to listOf(
                { db -> runScript(db, 14, "1. Player.sql") },
                { db -> updatePlayerStrategies(db) }
            ),
            14 to listOf(
                { db -> updatePlayerStrategiesToJson(db) },
                { db -> updateRoundTheClockParams(db) }
            ),
            15 to listOf (
                { db -> SyncAuditEntity(db).createTable() },
                { _ -> convertAchievement(ACHIEVEMENT_REF_X01_GAMES_WON) },
                { _ -> convertAchievement(ACHIEVEMENT_REF_GOLF_GAMES_WON) },
                { _ -> convertAchievement(ACHIEVEMENT_REF_CLOCK_GAMES_WON) },
                { _ -> convertAchievement(ACHIEVEMENT_REF_DARTZEE_GAMES_WON) }
            )
        )
    }

    /**
     * V14 -> V15
     */
    private fun convertAchievement(achievementRef: Int)
    {
        getAchievementForRef(achievementRef)!!.runConversion(emptyList())
    }
    private fun updatePlayerStrategiesToJson(database: Database)
    {
        val players = PlayerEntity(database).retrieveEntities("Strategy <> ''")
        players.forEach {
            val model = DartsAiModelOLD()
            model.readXml(it.strategy)

            val newModel = DartsAiModel(model.standardDeviation,
                if (model.standardDeviationDoubles > 0.0) model.standardDeviationDoubles else null,
                if (model.standardDeviationCentral > 0.0) model.standardDeviationCentral else null,
                450,
                model.scoringDart,
                model.hmScoreToDart.toMap(),
                if (model.mercyThreshold > -1) model.mercyThreshold else null,
                model.hmDartNoToSegmentType.toMap(),
                model.hmDartNoToStopThreshold.toMap(),
                model.dartzeePlayStyle)

            it.strategy = newModel.toJson()
            it.saveToDatabase()
        }
    }

    private fun updateRoundTheClockParams(database: Database)
    {
        val games = GameEntity(database).retrieveEntities("GameType = 'ROUND_THE_CLOCK'")
        games.forEach {
            val clockType = ClockType.valueOf(it.gameParams)
            val config = RoundTheClockConfig(clockType, true)
            it.gameParams = config.toJson()
            it.saveToDatabase()
        }
    }

    /**
     * V13 -> V14
     */
    private fun updatePlayerStrategies(database: Database)
    {
        val players = PlayerEntity(database).retrieveEntities("Strategy <> ''")
        players.forEach {
            val model = DartsAiModelOLD()
            model.readXml(it.strategy)
            it.strategy = model.writeXml()
            it.saveToDatabase()
        }
    }

    private fun runScript(database: Database, version: Int, scriptName: String): Boolean
    {
        val resourcePath = "/sql/v$version/"
        val rsrc = javaClass.getResource("$resourcePath$scriptName").readText()

        val batches = rsrc.split(";")

        return database.executeUpdates(batches)
    }
}
