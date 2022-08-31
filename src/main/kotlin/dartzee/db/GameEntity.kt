package dartzee.db

import dartzee.core.util.DateStatics
import dartzee.core.util.isEndOfTime
import dartzee.game.GameLaunchParams
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

const val MAX_PLAYERS = 6

/**
 * Represents a single game of Darts, e.g. X01 or Dartzee.
 */
class GameEntity(database: Database = mainDatabase): AbstractEntity<GameEntity>(database)
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

    override fun getTableName() = EntityName.Game

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

    override fun getColumnsAllowedToBeUnset() = listOf("DartsMatchId")

    override fun assignRowId(): String
    {
        localId = database.generateLocalId(getTableName())
        return super.assignRowId()
    }

    override fun reassignLocalId(otherDatabase: Database)
    {
        localId = otherDatabase.generateLocalId(getTableName())
    }

    /**
     * Helpers
     */
    fun isFinished() = !isEndOfTime(dtFinish)
    fun getTypeDesc() = gameType.getDescription(gameParams)

    fun retrievePlayersVector(): List<PlayerEntity>
    {
        val whereSql = "GameId = '$rowId' ORDER BY Ordinal ASC"
        val participants = ParticipantEntity().retrieveEntities(whereSql)
        return participants.map { it.getPlayer() }
    }

    companion object
    {
        fun factoryAndSave(launchParams: GameLaunchParams, match: DartsMatchEntity? = null): GameEntity
        {
            val game = factory(launchParams.gameType, launchParams.gameParams)
            match?.let {
                game.dartsMatchId = it.rowId
                game.matchOrdinal = 1
            }

            game.saveToDatabase()
            return game
        }

        fun factory(gameType: GameType, gameParams: String): GameEntity
        {
            val gameEntity = GameEntity()
            gameEntity.assignRowId()
            gameEntity.gameType = gameType
            gameEntity.gameParams = gameParams
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