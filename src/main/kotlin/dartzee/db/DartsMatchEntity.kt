package dartzee.db

import com.fasterxml.jackson.module.kotlin.readValue
import dartzee.core.util.*
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

/**
 * Simple entity to join multiple 'games' together into a 'match'.
 * Table has to be called 'DartsMatch' because 'Match' is a derby keyword!
 */
class DartsMatchEntity(database: Database = mainDatabase) : AbstractEntity<DartsMatchEntity>(database)
{
    /**
     * DB Fields
     */
    var localId = -1L
    var games = -1
    var mode: MatchMode = MatchMode.FIRST_TO
    var dtFinish = DateStatics.END_OF_TIME
    var matchParams = ""

    /**
     * Non-db gets / sets
     */
    var gameParams = ""
    var gameType: GameType = GameType.X01
    var players = mutableListOf<PlayerEntity>()

    private var currentOrdinal = 0
    private var hmPositionToPoints: Map<Int, Int>? = null

    override fun getTableName() = "DartsMatch"

    override fun getCreateTableSqlSpecific(): String
    {
        return "LocalId INT UNIQUE NOT NULL, Games INT NOT NULL, Mode VARCHAR(255) NOT NULL, DtFinish TIMESTAMP NOT NULL, MatchParams VARCHAR(500) NOT NULL"
    }

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
    fun isComplete() =
        when (mode)
        {
            MatchMode.FIRST_TO -> getIsFirstToMatchComplete()
            MatchMode.POINTS -> getIsPointsMatchComplete()
        }

    private fun getIsFirstToMatchComplete(): Boolean
    {
        val sb = StringBuilder()
        sb.append(" SELECT COUNT(1) AS WinCount")
        sb.append(" FROM Participant p, Game g")
        sb.append(" WHERE g.DartsMatchId = '$rowId'")
        sb.append(" AND p.GameId = g.RowId")
        sb.append(" AND p.FinishingPosition = 1")
        sb.append(" GROUP BY p.PlayerId")
        sb.append(" ORDER BY COUNT(1) DESC")
        sb.append(" FETCH FIRST 1 ROWS ONLY")

        database.executeQuery(sb).use { rs ->
            if (rs.next())
            {
                val count = rs.getInt("WinCount")
                return count == games
            }
        }

        return false
    }
    private fun getIsPointsMatchComplete(): Boolean
    {
        val sb = StringBuilder()
        sb.append(" SELECT COUNT(1)")
        sb.append(" FROM Game")
        sb.append(" WHERE DartsMatchId = '$rowId'")
        sb.append(" AND DtFinish < ")
        sb.append(getEndOfTimeSqlString())

        val count = database.executeQueryAggregate(sb)
        return count == games
    }

    fun getPlayerCount(): Int = players.size

    fun getMatchDesc(): String
    {
        return "Match #$localId (${getMatchTypeDesc()} - ${gameType.getDescription(gameParams)}, ${getPlayerCount()} players)"
    }

    private fun getMatchTypeDesc() =
        when(mode)
        {
            MatchMode.FIRST_TO -> "First to $games"
            MatchMode.POINTS -> "Points based ($games games)"
        }

    fun getScoreForFinishingPosition(position: Int) =
        when(mode)
        {
            MatchMode.FIRST_TO -> if (position == 1) 1 else 0
            MatchMode.POINTS -> if (position == -1) 0 else getHmPositionToPoints()[position]!!
        }

    private fun getHmPositionToPoints(): Map<Int, Int>
    {
        val result = hmPositionToPoints ?: jsonMapper().readValue(matchParams)
        hmPositionToPoints = result
        return result
    }

    fun incrementAndGetCurrentOrdinal() = ++currentOrdinal

    fun shufflePlayers()
    {
        if (players.size == 2)
        {
            players.reverse()
        }
        else
        {
            players.shuffle()
        }
    }

    fun cacheMetadataFromGame(lastGame: GameEntity)
    {
        this.gameType = lastGame.gameType
        this.gameParams = lastGame.gameParams
        this.players = lastGame.retrievePlayersVector()

        //Should've been setting this too...
        this.currentOrdinal = lastGame.matchOrdinal
    }

    companion object
    {
        fun constructPointsJson(first: Int, second: Int, third: Int, fourth: Int, fifth: Int, sixth: Int): String
        {
            val map = mapOf(1 to first, 2 to second, 3 to third, 4 to fourth, 5 to fifth, 6 to sixth)
            return jsonMapper().writeValueAsString(map)
        }

        /**
         * Factory methods
         */
        fun factoryFirstTo(games: Int): DartsMatchEntity
        {
            return factoryAndSave(games, MatchMode.FIRST_TO, "")
        }

        fun factoryPoints(games: Int, pointsJson: String): DartsMatchEntity
        {
            return factoryAndSave(games, MatchMode.POINTS, pointsJson)
        }

        private fun factoryAndSave(games: Int, mode: MatchMode, matchParams: String): DartsMatchEntity
        {
            val matchEntity = DartsMatchEntity()
            matchEntity.assignRowId()
            matchEntity.mode = mode
            matchEntity.games = games
            matchEntity.matchParams = matchParams
            matchEntity.saveToDatabase()
            return matchEntity
        }
    }
}
