package burlton.dartzee.code.stats;

import burlton.core.code.obj.SuperHashMap;
import burlton.core.code.util.Debug;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.dartzee.code.utils.DatabaseUtil;
import burlton.desktopcore.code.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PlayerSummaryStats
{
	private static SuperHashMap<String, PlayerSummaryStats> hmPlayerKeyToSummaryStats = new SuperHashMap<>();
	
	private int gameType = -1;
	
	private int gamesPlayed = -1;
	private int gamesWon = -1;
	private int bestScore = -1;
	private Timestamp lastPlayed = null;
	
	private PlayerSummaryStats(PlayerEntity player, int gameType)
	{
		this.gameType = gameType;
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(1), MAX(g.DtCreation), MIN(pt.FinalScore)");
		appendFromSql(sb, player);
		
		try (ResultSet rs = DatabaseUtil.executeQuery(sb))
		{	
			rs.next();
			
			gamesPlayed = rs.getInt(1);
			lastPlayed = rs.getTimestamp(2);
			bestScore = rs.getInt(3);
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(sb.toString(), sqle);
		}
		
		StringBuilder sbWon = new StringBuilder();
		sbWon.append("SELECT COUNT(1)");
		appendFromSql(sbWon, player);
		sbWon.append(" AND pt.FinishingPosition = 1");
		
		gamesWon = DatabaseUtil.executeQueryAggregate(sbWon);
	}
	private void appendFromSql(StringBuilder sb, PlayerEntity player)
	{
		sb.append(" FROM Game g, Participant pt");
		sb.append(" WHERE pt.GameId = g.RowId");
		sb.append(" AND g.GameType = ");
		sb.append(gameType);
		sb.append(" AND pt.PlayerId = ");
		sb.append(player.getRowId());
		sb.append(" AND pt.DtFinished <> ");
		sb.append(DateUtil.getEndOfTimeSqlString());
	}
	
	/**
	 * Gets / Sets
	 */
	public int getGamesPlayed()
	{
		return gamesPlayed;
	}
	public void setGamesPlayed(int gamesPlayed)
	{
		this.gamesPlayed = gamesPlayed;
	}
	public int getGamesWon()
	{
		return gamesWon;
	}
	public void setGamesWon(int gamesWon)
	{
		this.gamesWon = gamesWon;
	}
	public int getBestScore()
	{
		return bestScore;
	}
	public void setBestScore(int bestScore)
	{
		this.bestScore = bestScore;
	}
	public Timestamp getLastPlayed()
	{
		return lastPlayed;
	}
	public void setLastPlayed(Timestamp lastPlayed)
	{
		this.lastPlayed = lastPlayed;
	}
	
	/**
	 * Static methods
	 */
	public static PlayerSummaryStats getSummaryStats(PlayerEntity player, int gameType)
	{
		String key = player.getRowId() + "_" + gameType;
		
		PlayerSummaryStats stats = hmPlayerKeyToSummaryStats.get(key);
		if (stats != null)
		{
			return stats;
		}
		
		stats = new PlayerSummaryStats(player, gameType);
		hmPlayerKeyToSummaryStats.put(key, stats);
		return stats;
	}
	public static void resetPlayerStats(String playerId, int gameType)
	{
		String key = playerId + "_" + gameType;
		hmPlayerKeyToSummaryStats.remove(key);
	}
}
