package burlton.dartzee.code.reporting;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

import burlton.dartzee.code.db.PlayerEntity;
import burlton.core.code.obj.SuperHashMap;
import burlton.desktopcore.code.util.DateUtil;

public class ReportParameters
{
	private static final int MATCH_FILTER_MATCHES_ONLY = 0;
	private static final int MATCH_FILTER_GAMES_ONLY = 1;
	private static final int MATCH_FILTER_BOTH = 2;
	
	private int gameType = -1;
	private String gameParams = "";
	private boolean unfinishedOnly = false;
	private Timestamp dtStartFrom = null;
	private Timestamp dtStartTo = null;
	private Timestamp dtFinishFrom = null;
	private Timestamp dtFinishTo = null;
	private SuperHashMap<PlayerEntity, IncludedPlayerParameters> hmIncludedPlayerToParms = new SuperHashMap<>();
	private ArrayList<PlayerEntity> excludedPlayers = new ArrayList<>();
	private int partOfMatch = MATCH_FILTER_BOTH;
	
	public String getExtraWhereSql()
	{
		StringBuilder sb = new StringBuilder();
		
		if (gameType > -1)
		{
			sb.append(" AND g.GameType = ");
			sb.append(gameType);
		}
		
		if (!gameParams.isEmpty())
		{
			sb.append(" AND g.GameParams = '");
			sb.append(gameParams);
			sb.append("'");
		}
		
		if (dtStartFrom != null)
		{
			sb.append(" AND g.DtCreation >= '");
			sb.append(dtStartFrom);
			sb.append("'");
		}
		
		if (dtStartTo != null)
		{
			sb.append(" AND g.DtCreation <= '");
			sb.append(dtStartTo);
			sb.append("'");
		}
		
		if (dtFinishFrom != null)
		{
			sb.append(" AND g.DtFinish >= '");
			sb.append(dtFinishFrom);
			sb.append("'");
		}
		
		if (dtFinishTo != null)
		{
			sb.append(" AND g.DtFinish <= '");
			sb.append(dtFinishTo);
			sb.append("'");
		}
		
		if (unfinishedOnly)
		{
			sb.append(" AND g.DtFinish = ");
			sb.append(DateUtil.getEndOfTimeSqlString());
		}
		
		if (partOfMatch == MATCH_FILTER_GAMES_ONLY)
		{
			sb.append(" AND g.DartsMatchId = -1");
		}
		else if (partOfMatch == MATCH_FILTER_MATCHES_ONLY)
		{
			sb.append(" AND g.DartsMatchId > -1");
		}
		
		Iterator<Map.Entry<PlayerEntity, IncludedPlayerParameters>> it = hmIncludedPlayerToParms.entrySet().iterator();
		for (; it.hasNext(); )
		{
			Map.Entry<PlayerEntity, IncludedPlayerParameters> entry = it.next();
			PlayerEntity player = entry.getKey();
			IncludedPlayerParameters parms = entry.getValue();
			
			sb.append(" AND EXISTS (");
			sb.append(" SELECT 1 FROM Participant z");
			sb.append(" WHERE z.PlayerId = ");
			sb.append(player.getRowId());
			sb.append(" AND z.GameId = g.RowId");
			
			String extraSql = parms.generateExtraWhereSql("z");
			sb.append(extraSql);
			
			sb.append(")");
		}
		
		for (PlayerEntity player : excludedPlayers)
		{
			sb.append(" AND NOT EXISTS (");
			sb.append(" SELECT 1 FROM Participant z");
			sb.append(" WHERE z.PlayerId = ");
			sb.append(player.getRowId());
			sb.append(" AND z.GameId = g.RowId)");
		}
		
		return sb.toString();
	}
	
	public Predicate<ReportResultWrapper> getAsPredicate()
	{
		return (rr -> 
			(rr.getGameType() == gameType)
			&& (gameParams.isEmpty() || rr.getGameParams().equals(gameParams))
			&& (dtStartFrom == null || DateUtil.isOnOrAfter(rr.getDtStart(), dtStartFrom))
			&& (dtStartTo == null || DateUtil.isOnOrAfter(dtStartTo, rr.getDtStart()))
			&& (dtFinishFrom == null || DateUtil.isOnOrAfter(rr.getDtFinish(), dtFinishFrom))
			&& (dtFinishTo == null || DateUtil.isOnOrAfter(dtFinishTo, rr.getDtFinish()))
			);
	}
	
	@Override
	public String toString()
	{
		return "[" + gameType + ", " + gameParams + ", " + dtStartFrom + ", " + dtStartTo + ", " + dtFinishFrom + ", " + dtFinishTo + "]";
	}
	
	public ReportParameters factoryCopy()
	{
		ReportParameters rp = new ReportParameters();
		
		rp.gameType = gameType;
		rp.gameParams = gameParams;
		rp.unfinishedOnly = unfinishedOnly;
		rp.dtStartFrom = dtStartFrom;
		rp.dtStartTo = dtStartTo;
		rp.dtFinishFrom = dtFinishFrom;
		rp.dtFinishTo = dtFinishTo;
		rp.hmIncludedPlayerToParms = new SuperHashMap<>(hmIncludedPlayerToParms);
		rp.excludedPlayers = new ArrayList<>(excludedPlayers);
		rp.partOfMatch = partOfMatch;
		
		return rp;
	}
	
	/**
	 * Setters
	 */
	public int getGameType()
	{
		return gameType;
	}
	public void setGameType(int gameType)
	{
		this.gameType = gameType;
	}
	public void setGameParams(String gameParams)
	{
		this.gameParams = gameParams;
	}
	public void setUnfinishedOnly(boolean unfinishedOnly)
	{
		this.unfinishedOnly = unfinishedOnly;
	}
	public Timestamp getDtStartFrom()
	{
		return dtStartFrom;
	}
	public void setDtStartFrom(Timestamp dtStartFrom)
	{
		this.dtStartFrom = dtStartFrom;
	}
	public void setDtStartTo(Timestamp dtStartTo)
	{
		this.dtStartTo = dtStartTo;
	}
	public Timestamp getDtFinishFrom()
	{
		return dtFinishFrom;
	}
	public void setDtFinishFrom(Timestamp dtFinishFrom)
	{
		this.dtFinishFrom = dtFinishFrom;
	}
	public void setDtFinishTo(Timestamp dtFinishTo)
	{
		this.dtFinishTo = dtFinishTo;
	}
	public void setIncludedPlayers(SuperHashMap<PlayerEntity, IncludedPlayerParameters> hmIncludedPlayerToParms)
	{
		this.hmIncludedPlayerToParms = hmIncludedPlayerToParms;
	}
	public void setExcludedPlayers(ArrayList<PlayerEntity> excludedPlayers)
	{
		this.excludedPlayers = excludedPlayers;
	}
	public void setEnforceMatch(boolean matches)
	{
		if (matches)
		{
			partOfMatch = MATCH_FILTER_MATCHES_ONLY;
		}
		else
		{
			partOfMatch = MATCH_FILTER_GAMES_ONLY;
		}
	}
}
