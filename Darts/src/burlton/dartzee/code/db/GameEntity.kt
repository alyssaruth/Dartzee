package burlton.dartzee.code.db

import burlton.dartzee.code.bean.*
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.util.DateStatics
import burlton.desktopcore.code.util.isEndOfTime
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
    var localId = -1L
    var gameType = -1
    var gameParams = ""
    var dtFinish = DateStatics.END_OF_TIME
    var dartsMatchId: String = ""
    var matchOrdinal = -1


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

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>)
    {
        indexes.add(listOf("GameType"))
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

    companion object
    {
        @JvmStatic fun factoryAndSave(gameType: Int, gameParams: String): GameEntity
        {
            val gameEntity = GameEntity()
            gameEntity.assignRowId()
            gameEntity.gameType = gameType
            gameEntity.gameParams = gameParams
            gameEntity.saveToDatabase()
            return gameEntity
        }

        @JvmStatic fun factoryAndSave(match: DartsMatchEntity): GameEntity
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
         * Ordered by DtCreation as well because of an historic bug with loading where the ordinals could get screwed up.
         */
        @JvmStatic fun retrieveGamesForMatch(matchId: String): MutableList<GameEntity>
        {
            val sql = "DartsMatchId = '$matchId' ORDER BY MatchOrdinal, DtCreation"
            return GameEntity().retrieveEntities(sql)
        }

        @JvmStatic fun getTypeDesc(gameType: Int, gameParams: String): String
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

        @JvmStatic fun getTypeDesc(gameType: Int): String
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

        @JvmStatic fun getFilterPanel(gameType: Int): GameParamFilterPanel
        {
            return when (gameType)
            {
                GAME_TYPE_X01 -> GameParamFilterPanelX01()
                GAME_TYPE_GOLF -> GameParamFilterPanelGolf()
                GAME_TYPE_ROUND_THE_CLOCK -> GameParamFilterPanelRoundTheClock()
                else -> GameParamFilterPanelDartzee()
            }

        }

        @JvmStatic fun getAllGameTypes(): MutableList<Int>
        {
            return mutableListOf(GAME_TYPE_X01, GAME_TYPE_GOLF, GAME_TYPE_ROUND_THE_CLOCK, GAME_TYPE_DARTZEE)
        }

        @JvmStatic fun getGameId(localId: Long): String?
        {
            val game = GameEntity().retrieveEntity("LocalId = $localId") ?: return null
            return game.rowId
        }
    }
}