package dartzee.utils

import dartzee.db.DeletionAuditEntity
import dartzee.db.TeamEntity

object DatabaseMigrations
{
    fun getConversionsMap(): Map<Int, List<(database: Database) -> Any>>
    {
        return mapOf(
            18 to listOf { db -> createDeletionAudit(db) },
            19 to listOf (
                { db -> TeamEntity(db).createTable() },
                { db -> runScript(db, 20, "Participant.sql") }
            ),
            20 to listOf { db -> runScript(db, 21, "Dart.sql") }
        )
    }

    /**
     * V18 -> V19
     */
    fun createDeletionAudit(database: Database)
    {
        DeletionAuditEntity(database).createTable()
    }

    private fun runScript(database: Database, version: Int, scriptName: String): Boolean
    {
        val resourcePath = "/sql/v$version/"
        val rsrc = javaClass.getResource("$resourcePath$scriptName").readText()

        val batches = rsrc.split(";")

        return database.executeUpdates(batches)
    }
}
