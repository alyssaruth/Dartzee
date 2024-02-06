package dartzee.utils

import dartzee.db.GameEntity
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
            22 to listOf { db -> convertX01GameParams(db) }
        )
    }

    private fun convertX01GameParams(database: Database) {
        val games = GameEntity(database).retrieveEntities("GameType = '${GameType.X01}'")
        games.forEach {
            val target = it.gameParams.toInt()
            val config = X01Config(target, FinishType.Doubles)
            it.gameParams = config.toJson()
            it.saveToDatabase()
        }
    }

    private fun runScript(database: Database, version: Int, scriptName: String): Boolean {
        val resourcePath = "/sql/v$version/"
        val rsrc = javaClass.getResource("$resourcePath$scriptName").readText()

        val batches = rsrc.split(";")

        return database.executeUpdates(batches)
    }
}
