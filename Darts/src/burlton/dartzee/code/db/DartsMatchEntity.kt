package burlton.dartzee.code.db

import burlton.core.code.util.*
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.util.DateStatics
import burlton.desktopcore.code.util.getEndOfTimeSqlString

/**
 * Simple entity to join multiple 'games' together into a 'match'.
 * Table has to be called 'DartsMatch' because 'Match' is a derby keyword!
 */
class DartsMatchEntity : AbstractEntity<DartsMatchEntity>()
{
    /**
     * DB Fields
     */
    var localId = -1L
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

    override fun getTableName() = "DartsMatch"

    override fun getCreateTableSqlSpecific(): String
    {
        return "LocalId INT UNIQUE NOT NULL, Games INT NOT NULL, Mode INT NOT NULL, DtFinish TIMESTAMP NOT NULL, MatchParams VARCHAR(255) NOT NULL"
    }

    override fun assignRowId(): String
    {
        localId = LocalIdGenerator.generateLocalId(getTableName())
        return super.assignRowId()
    }

    /**
     * Helpers
     */
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

        DatabaseUtil.executeQuery(sb).use { rs ->
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

        val count = DatabaseUtil.executeQueryAggregate(sb)
        return count == games
    }

    fun getPlayerCount(): Int = players.size

    fun getMatchDesc(): String
    {
        return "Match #$localId (${getMatchTypeDesc()} - ${GameEntity.getTypeDesc(gameType, gameParams)}, ${getPlayerCount()} players)"
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
    private fun getHmPositionToPoints(): MutableMap<Int, Int>
    {
        if (hmPositionToPoints == null)
        {
            hmPositionToPoints = mutableMapOf()

            val doc = matchParams.toXmlDoc()
            val root = doc!!.documentElement

            hmPositionToPoints!![1] = root.getAttributeInt("First")
            hmPositionToPoints!![2] = root.getAttributeInt("Second")
            hmPositionToPoints!![3] = root.getAttributeInt("Third")
            hmPositionToPoints!![4] = root.getAttributeInt("Fourth")
        }

        return hmPositionToPoints!!
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
        const val MODE_FIRST_TO = 0
        const val MODE_POINTS = 1

        fun constructPointsXml(first: Int, second: Int, third: Int, fourth: Int): String
        {
            val doc = XmlUtil.factoryNewDocument()
            val rootElement = doc.createRootElement("MatchParams")
            rootElement.setAttribute("First", "$first")
            rootElement.setAttribute("Second", "$second")
            rootElement.setAttribute("Third", "$third")
            rootElement.setAttribute("Fourth", "$fourth")

            return doc.toXmlString()
        }


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
