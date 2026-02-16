package dartzee.db

import dartzee.core.util.DateStatics
import dartzee.core.util.getSqlDateNow
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

/**
 * Represents the participant of a game. This is a link between a player and a game, with additional
 * information such as play position and finishing position.
 */
class ParticipantEntity(database: Database = mainDatabase) :
    AbstractEntity<ParticipantEntity>(database), IParticipant {
    // DB Fields
    override var gameId = ""
    var playerId = ""
    override var ordinal = -1
    override var finishingPosition = -1
    override var finalScore = -1
    override var dtFinished = DateStatics.END_OF_TIME
    var teamId = ""
    override var resigned = false

    // In memory things
    private var player: PlayerEntity? = null

    override fun getTableName() = EntityName.Participant

    override fun getCreateTableSqlSpecific(): String {
        return ("GameId VARCHAR(36) NOT NULL, " +
            "PlayerId VARCHAR(36) NOT NULL, " +
            "Ordinal INT NOT NULL, " +
            "FinishingPosition INT NOT NULL, " +
            "FinalScore INT NOT NULL, " +
            "DtFinished TIMESTAMP NOT NULL, " +
            "TeamId VARCHAR(36) NOT NULL, " +
            "Resigned BOOLEAN NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>) {
        indexes.add(listOf("PlayerId", "GameId"))
    }

    override fun getColumnsAllowedToBeUnset() = listOf("TeamId")

    override fun saveToDatabase() = saveToDatabase(getSqlDateNow())

    /** Helpers */
    fun isAi() = getPlayer().isAi()

    fun getModel() = getPlayer().getModel()

    fun getPlayerName() = getPlayer().name

    /** Non-db Gets / Sets */
    fun getPlayer(): PlayerEntity {
        if (player == null) {
            player = PlayerEntity().retrieveForId(playerId)
        }

        return player!!
    }

    fun setPlayer(player: PlayerEntity) {
        this.player = player
    }

    companion object {
        fun factoryAndSave(
            gameId: String,
            player: PlayerEntity,
            ordinal: Int,
            teamId: String = "",
        ): ParticipantEntity {
            val gp = ParticipantEntity()
            gp.assignRowId()
            gp.gameId = gameId
            gp.playerId = player.rowId
            gp.ordinal = ordinal
            gp.teamId = teamId

            // Cache the actual player entity so we can access its strategy etc
            gp.setPlayer(player)

            gp.saveToDatabase()
            return gp
        }
    }
}
