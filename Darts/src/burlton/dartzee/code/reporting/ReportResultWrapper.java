package burlton.dartzee.code.reporting;

import burlton.core.code.obj.HandyArrayList;
import burlton.dartzee.code.db.GameEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Objects;

public class ReportResultWrapper
{
	private long localId = -1;
	private int gameType = -1;
	private String gameParams = null;
	private Timestamp dtStart = null;
	private Timestamp dtFinish = null;
	private HandyArrayList<ParticipantWrapper> participants = new HandyArrayList<>();
	private long localMatchId = -1;
	private int matchOrdinal = -1;
	
	
	public void addParticipant(ResultSet rs) throws SQLException
	{
		String playerName = rs.getString("Name");
		int finishPos = rs.getInt("FinishingPosition");
		participants.add(new ParticipantWrapper(playerName, finishPos));
	}
	
	public Object[] getTableRow()
	{
		String gameTypeDesc = GameEntity.getTypeDesc(gameType, gameParams);
		String playerDesc = getPlayerDesc();
		
		String matchDesc = "";
		if (localMatchId > -1)
		{
			matchDesc = "#" + localMatchId + " (Game " + (matchOrdinal+1) + ")";
		}
		
		Object[] row = {localId, gameTypeDesc, playerDesc, dtStart, dtFinish, matchDesc};
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
	
	public static ReportResultWrapper factoryFromResultSet(long localId, ResultSet rs) throws SQLException
	{
		ReportResultWrapper ret = new ReportResultWrapper();

		ret.localId = localId;
		ret.gameType = rs.getInt("GameType");
		ret.gameParams = rs.getString("GameParams");
		ret.dtStart = rs.getTimestamp("DtCreation");
		ret.dtFinish = rs.getTimestamp("DtFinish");
		
		ret.addParticipant(rs);
		
		ret.localMatchId = rs.getLong("LocalMatchId");
		ret.matchOrdinal = rs.getInt("MatchOrdinal");
		
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

	@Override
	public int hashCode() {
		return Objects.hash(localId);
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
		return localId == other.getLocalId();
	}

	/**
	 * Gets
	 */
	public long getLocalId() { return localId; }
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
	public int getMatchOrdinal()
	{
		return matchOrdinal;
	}
}
