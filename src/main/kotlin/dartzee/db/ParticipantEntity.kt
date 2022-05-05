package dartzee.db

import dartzee.achievements.getWinAchievementType
import dartzee.core.util.DateStatics
import dartzee.core.util.StringUtil
import dartzee.core.util.getSqlDateNow
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

/**
 * Represents the participant of a game. This is a link between a player and a game, with additional information
 * such as play position and finishing position.
 */
class ParticipantEntity(database: Database = mainDatabase): AbstractEntity<ParticipantEntity>(database), IParticipant
{
    //DB Fields
    var gameId = ""
    var playerId = ""
    var ordinal = -1
    override var finishingPosition = -1
    override var finalScore = -1
    override var dtFinished = DateStatics.END_OF_TIME
    var teamId = ""

    //In memory things
    private var player: PlayerEntity? = null

    override fun getTableName() = EntityName.Participant

    override fun getCreateTableSqlSpecific(): String
    {
        return ("GameId VARCHAR(36) NOT NULL, "
                + "PlayerId VARCHAR(36) NOT NULL, "
                + "Ordinal INT NOT NULL, "
                + "FinishingPosition INT NOT NULL, "
                + "FinalScore INT NOT NULL, "
                + "DtFinished TIMESTAMP NOT NULL, "
                + "TeamId VARCHAR(36) NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>)
    {
        indexes.add(listOf("PlayerId", "GameId"))
    }

    override fun getColumnsAllowedToBeUnset() = listOf("TeamId")

    override fun saveToDatabase() = saveToDatabase(getSqlDateNow())

    override fun saveFinishingPosition(game: GameEntity, position: Int)
    {
        super.saveFinishingPosition(game, position)

        if (position == 1)
        {
            val type = getWinAchievementType(game.gameType)
            AchievementEntity.insertAchievement(type, playerId, game.rowId, "$finalScore")
        }
    }

    /**
     * Helpers
     */
    fun isAi() = getPlayer().isAi()
    fun getFinishingPositionDesc(): String = StringUtil.convertOrdinalToText(finishingPosition)
    fun getModel() = getPlayer().getModel()
    fun getPlayerName() = getPlayer().name

    /**
     * Non-db Gets / Sets
     */
    fun getPlayer(): PlayerEntity
    {
        if (player == null)
        {
            player = PlayerEntity().retrieveForId(playerId)
        }

        return player!!
    }

    fun setPlayer(player: PlayerEntity)
    {
        this.player = player
    }

    companion object
    {
        fun factoryAndSave(gameId: String, player: PlayerEntity, ordinal: Int, teamId: String = ""): ParticipantEntity
        {
            val gp = ParticipantEntity()
            gp.assignRowId()
            gp.gameId = gameId
            gp.playerId = player.rowId
            gp.ordinal = ordinal
            gp.teamId = teamId

            //Cache the actual player entity so we can access its strategy etc
            gp.setPlayer(player)

            gp.saveToDatabase()
            return gp
        }
    }
}
