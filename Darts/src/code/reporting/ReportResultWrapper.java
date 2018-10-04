package code.reporting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;

import code.db.GameEntity;
import object.HandyArrayList;

public class ReportResultWrapper
{
	private long gameId = -1;
	private int gameType = -1;
	private String gameParams = null;
	private Timestamp dtStart = null;
	private Timestamp dtFinish = null;
	private HandyArrayList<ParticipantWrapper> participants = new HandyArrayList<>();
	private long matchId = -1;
	private int matchOrdinal = -1;
	
	
	public void addParticipant(ResultSet rs) throws SQLException
	{
		String playerName = rs.getString(6);
		int finishPos = rs.getInt(7);
		participants.add(new ParticipantWrapper(playerName, finishPos));
	}
	
	public Object[] getTableRow()
	{
		String gameTypeDesc = GameEntity.getTypeDesc(gameType, gameParams);
		String playerDesc = getPlayerDesc();
		
		String matchDesc = "";
		if (matchId > -1)
		{
			matchDesc = "#" + matchId + " (Game " + (matchOrdinal+1) + ")";
		}
		
		Object[] row = {gameId, gameTypeDesc, playerDesc, dtStart, dtFinish, matchDesc};
		return row;
	}
	private String getPlayerDesc()
	{
		participants.sort((ParticipantWrapper p1, ParticipantWrapper p2) -> Integer.compare(p1.getFinishingPosition(), p2.getFinishingPosition()));
		
		String desc = "";
		for (int i=0; i<participants.size(); i++)
		{
			if (i > 0)
			{
				desc += ", ";
			}
			
			desc += participants.get(i);
		}
		
		return desc;
	}
	
	public static ReportResultWrapper factoryFromResultSet(long gameId, ResultSet rs) throws SQLException
	{
		ReportResultWrapper ret = new ReportResultWrapper();
		
		ret.gameId = gameId;
		ret.gameType = rs.getInt(2);
		ret.gameParams = rs.getString(3);
		ret.dtStart = rs.getTimestamp(4);
		ret.dtFinish = rs.getTimestamp(5);
		
		ret.addParticipant(rs);
		
		
		ret.matchId = rs.getLong(8);
		ret.matchOrdinal = rs.getInt(9);
		
		return ret;
	}
	
	public static ArrayList<Object[]> getTableRowsFromWrappers(ArrayList<ReportResultWrapper> wrappers)
	{
		ArrayList<Object[]> rows = new ArrayList<>();
		for (ReportResultWrapper wrapper : wrappers)
		{
			rows.add(wrapper.getTableRow());
		}
		
		return rows;
	}
	
	public static Comparator<ReportResultWrapper> getComparator()
	{
		return (ReportResultWrapper r1, ReportResultWrapper r2) -> Long.compare(r1.getGameId(), r2.getGameId());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dtFinish == null) ? 0 : dtFinish.hashCode());
		result = prime * result + ((dtStart == null) ? 0 : dtStart.hashCode());
		result = prime * result + (int) (gameId ^ (gameId >>> 32));
		result = prime * result
				+ ((gameParams == null) ? 0 : gameParams.hashCode());
		result = prime * result + gameType;
		result = prime * result + (int) (matchId ^ (matchId >>> 32));
		result = prime * result + matchOrdinal;
		result = prime * result
				+ ((participants == null) ? 0 : participants.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ReportResultWrapper))
			return false;
		ReportResultWrapper other = (ReportResultWrapper) obj;
		return gameId == other.getGameId();
	}

	/**
	 * Gets
	 */
	public long getGameId()
	{
		return gameId;
	}
	public int getGameType()
	{
		return gameType;
	}
	public String getGameParams()
	{
		return gameParams;
	}
	public Timestamp getDtStart()
	{
		return dtStart;
	}
	public Timestamp getDtFinish()
	{
		return dtFinish;
	}
	public HandyArrayList<ParticipantWrapper> getParticipants()
	{
		return participants;
	}
	public long getMatchId()
	{
		return matchId;
	}
	public int getMatchOrdinal()
	{
		return matchOrdinal;
	}
}
