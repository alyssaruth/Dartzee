package dartzee.db

import dartzee.core.util.DateStatics
import dartzee.core.util.getSqlDateNow
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

/** Represents a team in a game. Individual participants will point at this entity in team games. */
class TeamEntity(database: Database = mainDatabase) :
    AbstractEntity<TeamEntity>(database), IParticipant {
    // DB Fields
    override var gameId = ""
    override var ordinal = -1
    override var finishingPosition = -1
    override var finalScore = -1
    override var dtFinished = DateStatics.END_OF_TIME
    override var resigned = false

    override fun getTableName() = EntityName.Team

    override fun getCreateTableSqlSpecific(): String {
        return ("GameId VARCHAR(36) NOT NULL, " +
            "Ordinal INT NOT NULL, " +
            "FinishingPosition INT NOT NULL, " +
            "FinalScore INT NOT NULL, " +
            "DtFinished TIMESTAMP NOT NULL, " +
            "Resigned BOOLEAN NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>) {
        indexes.add(listOf("GameId"))
    }

    override fun saveToDatabase() = saveToDatabase(getSqlDateNow())

    companion object {
        fun factoryAndSave(gameId: String, ordinal: Int): TeamEntity {
            val team = TeamEntity()
            team.assignRowId()
            team.gameId = gameId
            team.ordinal = ordinal
            team.saveToDatabase()
            return team
        }
    }
}
