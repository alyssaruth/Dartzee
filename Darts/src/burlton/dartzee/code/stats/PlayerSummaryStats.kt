package burlton.dartzee.code.stats

import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.core.util.getEndOfTimeSqlString
import java.sql.Timestamp

class PlayerSummaryStats private constructor(player: PlayerEntity, private val gameType: Int)
{
    var gamesPlayed = -1
    var gamesWon = -1
    var bestScore = -1
    var lastPlayed: Timestamp? = null

    init
    {
        val sb = StringBuilder()
        sb.append("SELECT COUNT(1), MAX(g.DtCreation), MIN(pt.FinalScore)")
        appendFromSql(sb, player)

        DatabaseUtil.executeQuery(sb).use { rs ->
            rs.next()

            gamesPlayed = rs.getInt(1)
            lastPlayed = rs.getTimestamp(2)
            bestScore = rs.getInt(3)
        }

        val sbWon = StringBuilder()
        sbWon.append("SELECT COUNT(1)")
        appendFromSql(sbWon, player)
        sbWon.append(" AND pt.FinishingPosition = 1")

        gamesWon = DatabaseUtil.executeQueryAggregate(sbWon)
    }

    private fun appendFromSql(sb: StringBuilder, player: PlayerEntity)
    {
        sb.append(" FROM Game g, Participant pt")
        sb.append(" WHERE pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $gameType")
        sb.append(" AND pt.PlayerId = '${player.rowId}'")
        sb.append(" AND pt.DtFinished <> ${getEndOfTimeSqlString()}")
    }

    companion object
    {
        private val hmPlayerKeyToSummaryStats = mutableMapOf<String, PlayerSummaryStats>()

        /**
         * Static methods
         */
        fun getSummaryStats(player: PlayerEntity, gameType: Int): PlayerSummaryStats
        {
            val key = player.rowId + "_" + gameType

            val stats = hmPlayerKeyToSummaryStats[key] ?: PlayerSummaryStats(player, gameType)
            hmPlayerKeyToSummaryStats[key] = stats
            return stats
        }

        fun resetPlayerStats(playerId: String, gameType: Int)
        {
            val key = playerId + "_" + gameType
            hmPlayerKeyToSummaryStats.remove(key)
        }
    }
}
