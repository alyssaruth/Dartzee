package dartzee.db

import dartzee.core.util.DateStatics
import dartzee.core.util.isEndOfTime
import dartzee.game.GameType
import dartzee.utils.DatabaseUtil
import java.util.*

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
    var gameType: GameType = GameType.X01
    var gameParams = ""
    var dtFinish = DateStatics.END_OF_TIME
    var dartsMatchId: String = ""
    var matchOrdinal = -1

    override fun getTableName() = "Game"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("LocalId INT UNIQUE NOT NULL, "
                + "GameType varchar(255) NOT NULL, "
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
    fun getTypeDesc() = gameType.getDescription(gameParams)

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
        fun factoryAndSave(gameType: GameType, gameParams: String): GameEntity
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
         * Ordered by DtCreation as well because of an historic bug with loading where the ordinals could get screwed up.
         */
        fun retrieveGamesForMatch(matchId: String): MutableList<GameEntity>
        {
            val sql = "DartsMatchId = '$matchId' ORDER BY MatchOrdinal, DtCreation"
            return GameEntity().retrieveEntities(sql)
        }

        fun getGameId(localId: Long): String?
        {
            val game = GameEntity().retrieveEntity("LocalId = $localId") ?: return null
            return game.rowId
        }
    }
}