package dartzee.utils

import dartzee.db.TeamEntity

object DatabaseMigrations {
    fun getConversionsMap(): Map<Int, List<(database: Database) -> Any>> {
        return mapOf(
            19 to
                listOf(
                    { db -> TeamEntity(db).createTable() },
                    { db -> runScript(db, 20, "Participant.sql") }
                ),
            20 to listOf { db -> runScript(db, 21, "Dart.sql") },
            21 to listOf { db -> runScript(db, 22, "Dart.sql") }
        )
    }

    private fun runScript(database: Database, version: Int, scriptName: String): Boolean {
        val resourcePath = "/sql/v$version/"
        val rsrc = javaClass.getResource("$resourcePath$scriptName").readText()

        val batches = rsrc.split(";")

        return database.executeUpdates(batches)
    }
}
