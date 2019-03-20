package burlton.dartzee.code.db

import burlton.core.code.obj.HandyArrayList
import burlton.core.code.util.Debug
import burlton.core.code.util.XmlUtil
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.util.DateStatics
import burlton.desktopcore.code.util.getEndOfTimeSqlString
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Simple entity to join multiple 'games' together into a 'match'.
 * Table has to be called 'DartsMatch' because 'Match' is a derby keyword!
 */
class DartsMatchEntity : AbstractEntity<DartsMatchEntity>()
{
    /**
     * DB Fields
     */
    var localId = -1
    var games = -1
    var mode = -1
    var dtFinish = DateStatics.END_OF_TIME
    var matchParams = ""

    /**
     * Non-db gets / sets
     */
    var gameParams = ""
    var gameType = -1
    var players = mutableListOf<PlayerEntity>()

    private var currentOrdinal = 0
    private var hmPositionToPoints: MutableMap<Int, Int>? = null

    fun isComplete(): Boolean
    {
        return when(mode)
        {
            MODE_FIRST_TO -> getIsFirstToMatchComplete()
            MODE_POINTS -> getIsPointsMatchComplete()
            else -> {
                Debug.stackTrace("Unimplemented for match mode [$mode]")
                false
            }
        }
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

        try
        {
            DatabaseUtil.executeQuery(sb).use { rs ->
                if (rs.next())
                {
                    val count = rs.getInt("WinCount")
                    return count == games
                }
            }
        }
        catch (sqle: SQLException)
        {
            Debug.logSqlException(sb.toString(), sqle)
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

        val count = DatabaseUtil.executeQueryAggregate(sb).toLong()
        return count == games.toLong()
    }

    fun getPlayerCount(): Int = players.size

    fun getMatchDesc(): String
    {
        return "Match #$localId (${getMatchTypeDesc()} - ${getTypeDesc(gameType, gameParams)}, ${getPlayerCount()} players)"
    }

    private fun getMatchTypeDesc(): String
    {
        return when(mode)
        {
            MODE_FIRST_TO -> "First to $games"
            MODE_POINTS -> "Points based ($games games)"
            else -> ""
        }
    }

    override fun getTableName() = "DartsMatch"

    override fun getCreateTableSqlSpecific(): String
    {
        return "LocalId INT NOT NULL, Games INT NOT NULL, Mode INT NOT NULL, DtFinish TIMESTAMP NOT NULL, MatchParams VARCHAR(255) NOT NULL"
    }

    @Throws(SQLException::class)
    override fun populateFromResultSet(entity: DartsMatchEntity, rs: ResultSet)
    {
        entity.localId = rs.getInt("LocalId")
        entity.games = rs.getInt("Games")
        entity.mode = rs.getInt("Mode")
        entity.dtFinish = rs.getTimestamp("DtFinish")
        entity.matchParams = rs.getString("MatchParams")
    }

    @Throws(SQLException::class)
    override fun writeValuesToStatement(statement: PreparedStatement, startIndex: Int, emptyStatement: String): String
    {
        var i = startIndex
        var statementStr = emptyStatement
        statementStr = writeInt(statement, i++, localId, statementStr)
        statementStr = writeInt(statement, i++, games, statementStr)
        statementStr = writeInt(statement, i++, mode, statementStr)
        statementStr = writeTimestamp(statement, i++, dtFinish, statementStr)
        statementStr = writeString(statement, i, matchParams, statementStr)
        return statementStr
    }

    /**
     * Helpers
     */
    fun getScoreForFinishingPosition(position: Int): Int
    {
        return when(mode)
        {
            MODE_FIRST_TO -> if (position == 1) 1 else 0
            MODE_POINTS -> if (position == -1) 0 else getHmPositionToPoints()[position]!!
            else -> {
                Debug.stackTrace("Unexpected mode [$mode]")
                -1
            }
        }
    }

    fun incrementAndGetCurrentOrdinal() = currentOrdinal++

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

    private fun getHmPositionToPoints(): MutableMap<Int, Int>
    {
        if (hmPositionToPoints == null)
        {
            hmPositionToPoints = mutableMapOf()

            val doc = XmlUtil.getDocumentFromXmlString(matchParams)
            val root = doc!!.documentElement

            hmPositionToPoints!![1] = XmlUtil.getAttributeInt(root, "First")
            hmPositionToPoints!![2] = XmlUtil.getAttributeInt(root, "Second")
            hmPositionToPoints!![3] = XmlUtil.getAttributeInt(root, "Third")
            hmPositionToPoints!![4] = XmlUtil.getAttributeInt(root, "Fourth")
        }

        return hmPositionToPoints!!
    }

    fun cacheMetadataFromGame(lastGame: GameEntity)
    {
        this.gameType = lastGame.gameType
        this.gameParams = lastGame.gameParams
        this.players = HandyArrayList(lastGame.retrievePlayersVector())

        //Should've been setting this too...
        this.currentOrdinal = lastGame.matchOrdinal
    }

    companion object
    {
        const val MODE_FIRST_TO = 0
        const val MODE_POINTS = 1

        /**
         * Factory methods
         */
        fun factoryFirstTo(games: Int): DartsMatchEntity
        {
            return factoryAndSave(games, MODE_FIRST_TO, "")
        }

        fun factoryPoints(games: Int, pointsXml: String): DartsMatchEntity
        {
            return factoryAndSave(games, MODE_POINTS, pointsXml)
        }

        private fun factoryAndSave(games: Int, mode: Int, matchParams: String): DartsMatchEntity
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
