package code.reporting;

import java.sql.ResultSet;
import java.sql.SQLException;

import code.utils.DatabaseUtil;
import object.HandyArrayList;
import object.SuperHashMap;
import util.Debug;
import util.DialogUtil;

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
				long gameId = rs.getLong(1);
				
				ReportResultWrapper wrapper = hm.get(gameId);
				if (wrapper != null)
				{
					wrapper.addParticipant(rs);
				}
				else
				{
					wrapper = ReportResultWrapper.factoryFromResultSet(gameId, rs);
					hm.put(gameId, wrapper);
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
		sb.append("SELECT g.RowId, g.GameType, g.GameParams, g.DtCreation, g.DtFinish, p.Name, pt.FinishingPosition, g.DartsMatchId, g.MatchOrdinal");
		sb.append(" FROM Game g, Participant pt, Player p");
		sb.append(" WHERE pt.GameId = g.RowId");
		sb.append(" AND pt.PlayerId = p.RowId");
		
		return sb.toString();
	}
}
