package burlton.dartzee.code.db

import burlton.core.code.util.StringUtil
import burlton.desktopcore.code.util.DateStatics
import burlton.desktopcore.code.util.isEndOfTime
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Represents the participant of a game. This is a link between a player and a game, with additional information
 * such as play position and finishing position.
 */
class ParticipantEntity : AbstractEntity<ParticipantEntity>()
{
    //DB Fields
    var gameId = ""
    var playerId = ""
    var ordinal = -1
    var finishingPosition = -1
    var finalScore = -1
    var dtFinished = DateStatics.END_OF_TIME

    //In memory things
    private var player: PlayerEntity? = null


    override fun getTableName() = "Participant"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("GameId VARCHAR(36) NOT NULL, "
                + "PlayerId VARCHAR(36) NOT NULL, "
                + "Ordinal INT NOT NULL, "
                + "FinishingPosition INT NOT NULL, "
                + "FinalScore INT NOT NULL, "
                + "DtFinished TIMESTAMP NOT NULL")
    }

    @Throws(SQLException::class)
    override fun populateFromResultSet(entity: ParticipantEntity, rs: ResultSet)
    {
        entity.gameId = rs.getString("GameId")
        entity.playerId = rs.getString("PlayerId")
        entity.ordinal = rs.getInt("Ordinal")
        entity.finishingPosition = rs.getInt("FinishingPosition")
        entity.finalScore = rs.getInt("FinalScore")
        entity.dtFinished = rs.getTimestamp("DtFinished")
    }

    @Throws(SQLException::class)
    override fun writeValuesToStatement(statement: PreparedStatement, startIndex: Int, emptyStatement: String): String
    {
        var i = startIndex
        var statementStr = emptyStatement
        statementStr = writeString(statement, i++, gameId, statementStr)
        statementStr = writeString(statement, i++, playerId, statementStr)
        statementStr = writeInt(statement, i++, ordinal, statementStr)
        statementStr = writeInt(statement, i++, finishingPosition, statementStr)
        statementStr = writeInt(statement, i++, finalScore, statementStr)
        statementStr = writeTimestamp(statement, i, dtFinished, statementStr)

        return statementStr
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<MutableList<String>>)
    {
        val playerIdGameId = mutableListOf("PlayerId", "GameId")
        indexes.add(playerIdGameId)
    }

    override fun toString() = "Player $ordinal in Game #$gameId [PlayerId $playerId]"

    /**
     * Helpers
     */
    fun isAi() = getPlayer().isAi()
    fun isActive() = isEndOfTime(dtFinished)
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
        @JvmStatic fun factoryAndSave(gameId: String, player: PlayerEntity, ordinal: Int): ParticipantEntity
        {
            val gp = ParticipantEntity()
            gp.assignRowId()
            gp.gameId = gameId
            gp.playerId = player.rowId
            gp.ordinal = ordinal

            //Cache the actual player entity so we can access its strategy etc
            gp.setPlayer(player)

            gp.saveToDatabase()
            return gp
        }
    }
}
