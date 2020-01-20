package burlton.dartzee.code.reporting

import burlton.desktopcore.code.util.Debug
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.util.DialogUtil
import java.sql.SQLException

/**
 * Refactored from ReportingResultsScreen
 */
fun runReport(rp: ReportParameters?): List<ReportResultWrapper>
{
    rp ?: return listOf()

    var sql = buildBasicSqlStatement()
    sql += rp.getExtraWhereSql()

    val hm = mutableMapOf<Long, ReportResultWrapper>()
    try
    {
        DatabaseUtil.executeQuery(sql).use { rs ->
            while (rs.next())
            {
                val localId = rs.getLong("LocalId")

                var wrapper = hm[localId]
                if (wrapper != null)
                {
                    wrapper.addParticipant(rs)
                }
                else
                {
                    wrapper = ReportResultWrapper.factoryFromResultSet(localId, rs)
                    hm[localId] = wrapper
                }
            }
        }
    }
    catch (sqle: SQLException)
    {
        Debug.logSqlException(sql, sqle)
        DialogUtil.showError("SQL failed for report parameters.")

        //return an empty hashmap so we show an empty table
        hm.clear()
    }

    return hm.values.toList()
}

private fun buildBasicSqlStatement(): String
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
