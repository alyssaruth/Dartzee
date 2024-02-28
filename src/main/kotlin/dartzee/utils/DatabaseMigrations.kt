package dartzee.utils

import com.fasterxml.jackson.module.kotlin.readValue
import dartzee.core.util.jsonMapper
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.db.TeamEntity
import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.X01Config

object DatabaseMigrations {
    fun getConversionsMap(): Map<Int, List<(database: Database) -> Any>> {
        return mapOf(
            19 to
                listOf(
                    { db -> TeamEntity(db).createTable() },
                    { db -> runScript(db, 20, "Participant.sql") }
                ),
            20 to listOf { db -> runScript(db, 21, "Dart.sql") },
            21 to listOf { db -> runScript(db, 22, "Dart.sql") },
            22 to
                listOf(
                    ::convertX01GameParams,
                    ::dropHmScoreToDarts,
                    { db -> runScript(db, 23, "Participant.sql") },
                    { db -> runScript(db, 23, "Team.sql") }
                )
        )
    }

    private fun dropHmScoreToDarts(database: Database) {
        val players = PlayerEntity(database).retrieveEntities("Strategy != ''")
        players.forEach { player ->
            val strategyMap = jsonMapper().readValue<MutableMap<Any, Any>>(player.strategy)
            strategyMap.remove("hmScoreToDart")
            player.strategy = jsonMapper().writeValueAsString(strategyMap)
            player.saveToDatabase()
        }
    }

    private fun convertX01GameParams(database: Database) {
        val games = GameEntity(database).retrieveEntities("GameType = '${GameType.X01}'")
        games.forEach { game ->
            val target = game.gameParams.toInt()
            val config = X01Config(target, FinishType.Doubles)
            game.gameParams = config.toJson()
            game.saveToDatabase()
        }
    }

    private fun runScript(database: Database, version: Int, scriptName: String): Boolean {
        val resourcePath = "/sql/v$version/"
        val rsrc = javaClass.getResource("$resourcePath$scriptName").readText()

        val batches = rsrc.split(";")

        return database.executeUpdates(batches)
    }
}
