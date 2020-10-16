package dartzee.reporting

import dartzee.game.GameType
import dartzee.utils.InjectedThings.mainDatabase
import java.sql.ResultSet
import java.sql.Timestamp

/**
 * Refactored from ReportingResultsScreen
 */
fun runReport(rp: ReportParameters?): List<ReportResultWrapper>
{
    rp ?: return listOf()

    var sql = buildBasicSqlStatement()
    sql += rp.getExtraWhereSql()

    val hm = mutableMapOf<Long, ReportResultWrapper>()
    mainDatabase.executeQuery(sql).use { rs ->
        while (rs.next())
        {
            val localId = rs.getLong("LocalId")
            val wrapper = hm.getOrPut(localId) { ReportResultWrapper.factoryFromResultSet(localId, rs) }
            wrapper.addParticipant(rs)
        }
    }

    return hm.values.toList()
}

fun buildBasicSqlStatement(): String
{
    val sb = StringBuilder()
    sb.append("SELECT g.RowId, g.LocalId, g.GameType, g.GameParams, g.DtCreation, g.DtFinish, p.Name, pt.FinishingPosition, g.DartsMatchId, g.MatchOrdinal, ")
    sb.append(" CASE WHEN m.LocalId IS NULL THEN -1 ELSE m.LocalId END AS LocalMatchId")
    sb.append(" FROM Participant pt, Player p, Game g")
    sb.append(" LEFT OUTER JOIN DartsMatch m ON (g.DartsMatchId = m.RowId)")
    sb.append(" WHERE pt.GameId = g.RowId")
    sb.append(" AND pt.PlayerId = p.RowId")

    return sb.toString()
}

data class ReportResultWrapper(val localId: Long,
                               val gameType: GameType,
                               val gameParams: String,
                               val dtStart: Timestamp,
                               val dtFinish: Timestamp,
                               val localMatchId: Long,
                               val matchOrdinal: Int)
{
    private val participants = mutableListOf<ParticipantWrapper>()

    fun getTableRow(): Array<Any>
    {
        val gameTypeDesc = gameType.getDescription(gameParams)
        val playerDesc = getPlayerDesc()

        var matchDesc = ""
        if (localMatchId > -1)
        {
            matchDesc = "#$localMatchId (Game $matchOrdinal)"
        }

        return arrayOf(localId, gameTypeDesc, playerDesc, dtStart, dtFinish, matchDesc)
    }

    private fun getPlayerDesc(): String
    {
        participants.sortBy { it.finishingPosition }
        return participants.joinToString()
    }

    fun addParticipant(rs: ResultSet)
    {
        val playerName = rs.getString("Name")
        val finishPos = rs.getInt("FinishingPosition")
        participants.add(ParticipantWrapper(playerName, finishPos))
    }

    companion object
    {
        fun factoryFromResultSet(localId: Long, rs: ResultSet): ReportResultWrapper
        {
            val gameType = GameType.valueOf(rs.getString("GameType"))
            val gameParams = rs.getString("GameParams")
            val dtStart = rs.getTimestamp("DtCreation")
            val dtFinish = rs.getTimestamp("DtFinish")
            val localMatchId = rs.getLong("LocalMatchId")
            val matchOrdinal = rs.getInt("MatchOrdinal")

            return ReportResultWrapper(localId, gameType, gameParams, dtStart, dtFinish, localMatchId, matchOrdinal)
        }

        fun getTableRowsFromWrappers(wrappers: List<ReportResultWrapper>) = wrappers.map { it.getTableRow() }
    }
}