package burlton.dartzee.code.db

import burlton.dartzee.code.bean.GameParamFilterPanel
import burlton.dartzee.code.bean.GameParamFilterPanelGolf
import burlton.dartzee.code.bean.GameParamFilterPanelRoundTheClock
import burlton.dartzee.code.bean.GameParamFilterPanelX01
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.util.DateStatics
import burlton.desktopcore.code.util.isEndOfTime
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*


const val GAME_TYPE_X01 = 1
const val GAME_TYPE_GOLF = 2
const val GAME_TYPE_ROUND_THE_CLOCK = 3
const val GAME_TYPE_DARTZEE = 4

const val CLOCK_TYPE_STANDARD = "Standard"
const val CLOCK_TYPE_DOUBLES = "Doubles"
const val CLOCK_TYPE_TREBLES = "Trebles"

/**
 * Represents a single game of Darts, e.g. X01 or Dartzee.
 */
class GameEntity : AbstractEntity<GameEntity>()
{
    /**
     * DB fields
     */
    @JvmField var localId = -1L
    @JvmField var gameType = -1
    @JvmField var gameParams = ""
    @JvmField var dtFinish = DateStatics.END_OF_TIME
    @JvmField var dartsMatchId: String = ""
    @JvmField var matchOrdinal = -1

    /**
     * Helpers
     */
    fun getParticipantCount(): Int
    {
        val sb = StringBuilder()
        sb.append("SELECT COUNT(1) FROM ")
        sb.append(ParticipantEntity().getTableName())
        sb.append(" WHERE GameId = '$rowId'")

        return DatabaseUtil.executeQueryAggregate(sb)
    }

    fun isFinished() = !isEndOfTime(dtFinish)
    fun getTypeDesc() = getTypeDesc(gameType, gameParams)
    override fun getTableName() = "Game"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("LocalId INT UNIQUE NOT NULL, "
                + "GameType INT NOT NULL, "
                + "GameParams varchar(255) NOT NULL, "
                + "DtFinish timestamp NOT NULL, "
                + "DartsMatchId VARCHAR(36) NOT NULL, "
                + "MatchOrdinal INT NOT NULL")
    }

    @Throws(SQLException::class)
    override fun populateFromResultSet(entity: GameEntity, rs: ResultSet)
    {
        entity.localId = rs.getLong("LocalId")
        entity.gameType = rs.getInt("GameType")
        entity.gameParams = rs.getString("GameParams")
        entity.dtFinish = rs.getTimestamp("DtFinish")
        entity.dartsMatchId = rs.getString("DartsMatchId")
        entity.matchOrdinal = rs.getInt("MatchOrdinal")
    }

    @Throws(SQLException::class)
    override fun writeValuesToStatement(statement: PreparedStatement, startIndex: Int, emptyStatement: String): String
    {
        var i = startIndex
        var statementStr = emptyStatement
        statementStr = writeLong(statement, i++, localId, statementStr)
        statementStr = writeInt(statement, i++, gameType, statementStr)
        statementStr = writeString(statement, i++, gameParams, statementStr)
        statementStr = writeTimestamp(statement, i++, dtFinish, statementStr)
        statementStr = writeString(statement, i++, dartsMatchId, statementStr)
        statementStr = writeInt(statement, i, matchOrdinal, statementStr)

        return statementStr
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<MutableList<String>>)
    {
        val gameTypeIndex = mutableListOf<String>()
        gameTypeIndex.add("GameType")
        indexes.add(gameTypeIndex)
    }

    override fun getColumnsAllowedToBeUnset(): ArrayList<String>
    {
        val ret = ArrayList<String>()
        ret.add("DartsMatchId")
        return ret
    }

    override fun assignRowId(): String
    {
        localId = LocalIdGenerator.generateLocalId(getTableName())
        return super.assignRowId()
    }

    fun retrievePlayersVector(): MutableList<PlayerEntity>
    {
        val ret = mutableListOf<PlayerEntity>()

        val whereSql = "GameId = '$rowId' ORDER BY Ordinal ASC"
        val participants = ParticipantEntity().retrieveEntities(whereSql)

        participants.forEach{
            ret.add(it.getPlayer())
        }

        return ret
    }
}

/**
 * Top-level methods
 */
fun factoryAndSave(gameType: Int, gameParams: String): GameEntity
{
    val gameEntity = GameEntity()
    gameEntity.assignRowId()
    gameEntity.gameType = gameType
    gameEntity.gameParams = gameParams
    gameEntity.saveToDatabase()
    return gameEntity
}

fun factoryAndSave(match: DartsMatchEntity): GameEntity
{
    val gameEntity = GameEntity()
    gameEntity.assignRowId()
    gameEntity.gameType = match.gameType
    gameEntity.gameParams = match.gameParams
    gameEntity.dartsMatchId = match.rowId
    gameEntity.matchOrdinal = match.incrementAndGetCurrentOrdinal()
    gameEntity.saveToDatabase()
    return gameEntity
}

/**
 * Ordered by RowId as well because of a bug with loading where the ordinals could get screwed up.
 */
fun retrieveGamesForMatch(matchId: String): MutableList<GameEntity>
{
    val sql = "DartsMatchId = '$matchId' ORDER BY MatchOrdinal, DtCreation"
    return GameEntity().retrieveEntities(sql)
}

fun getTypeDesc(gameType: Int, gameParams: String): String
{
    return when(gameType)
    {
        GAME_TYPE_X01 -> gameParams
        GAME_TYPE_GOLF -> "Golf - $gameParams holes"
        GAME_TYPE_ROUND_THE_CLOCK -> "Round the Clock - $gameParams"
        GAME_TYPE_DARTZEE -> "Dartzee"
        else -> ""
    }
}

fun getTypeDesc(gameType: Int): String
{
    return when (gameType)
    {
        GAME_TYPE_X01 -> "X01"
        GAME_TYPE_GOLF -> "Golf"
        GAME_TYPE_ROUND_THE_CLOCK -> "Round the Clock"
        GAME_TYPE_DARTZEE -> "Dartzee"
        else -> "<Game Type>"
    }
}

fun getFilterPanel(gameType: Int): GameParamFilterPanel?
{
    return when (gameType)
    {
        GAME_TYPE_X01 -> GameParamFilterPanelX01()
        GAME_TYPE_GOLF -> GameParamFilterPanelGolf()
        GAME_TYPE_ROUND_THE_CLOCK -> GameParamFilterPanelRoundTheClock()
        else -> null
    }

}

fun getAllGameTypes(): MutableList<Int>
{
    return mutableListOf(GAME_TYPE_X01, GAME_TYPE_GOLF, GAME_TYPE_ROUND_THE_CLOCK)
}

fun getGameId(localId: Long): String?
{
    val game = GameEntity().retrieveEntity("LocalId = $localId") ?: return null
    return game.rowId
}