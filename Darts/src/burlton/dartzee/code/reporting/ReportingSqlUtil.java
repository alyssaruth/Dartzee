package burlton.dartzee.code.reporting;

import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.obj.SuperHashMap;
import burlton.core.code.util.Debug;
import burlton.dartzee.code.utils.DatabaseUtil;
import burlton.desktopcore.code.util.DialogUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Refactored from ReportingResultsScreen
 */
public final class ReportingSqlUtil
{
	public static HandyArrayList<ReportResultWrapper> runReport(ReportParameters rp)
	{
		String sql = buildBasicSqlStatement();
		sql += rp.getExtraWhereSql();
		
		SuperHashMap<Long, ReportResultWrapper> hm = new SuperHashMap<>();
		try (ResultSet rs = DatabaseUtil.executeQuery(sql))
		{
			while (rs.next())
			{
				long localId = rs.getLong("LocalId");
				
				ReportResultWrapper wrapper = hm.get(localId);
				if (wrapper != null)
				{
					wrapper.addParticipant(rs);
				}
				else
				{
					wrapper = ReportResultWrapper.factoryFromResultSet(localId, rs);
					hm.put(localId, wrapper);
				}
			}
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(sql, sqle);
			DialogUtil.showError("SQL failed for report parameters.");
			
			//return an empty hashmap so we show an empty table
			hm.clear();
		}
		
		return hm.getValuesAsVector();
	}

	private static String buildBasicSqlStatement()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT g.RowId, g.LocalId, g.GameType, g.GameParams, g.DtCreation, g.DtFinish, p.Name, pt.FinishingPosition, g.DartsMatchId, g.MatchOrdinal, ");
		sb.append(" CASE WHEN m.LocalId IS NULL THEN -1 ELSE m.LocalId END AS LocalMatchId");
		sb.append(" FROM Participant pt, Player p, Game g");
		sb.append(" LEFT OUTER JOIN DartsMatch m ON (g.DartsMatchId = m.RowId)");
		sb.append(" WHERE pt.GameId = g.RowId");
		sb.append(" AND pt.PlayerId = p.RowId");
		
		return sb.toString();
	}
}
