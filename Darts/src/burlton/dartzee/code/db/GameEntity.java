package burlton.dartzee.code.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import burlton.dartzee.code.bean.GameParamFilterPanel;
import burlton.dartzee.code.bean.GameParamFilterPanelGolf;
import burlton.dartzee.code.bean.GameParamFilterPanelRoundTheClock;
import burlton.dartzee.code.bean.GameParamFilterPanelX01;
import burlton.dartzee.code.utils.DatabaseUtil;
import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.util.ClassUtil;
import burlton.desktopcore.code.util.DateUtil;

/**
 * Represents a single game of Darts, e.g. X01 or Dartzee.
 */
public class GameEntity extends AbstractDartsEntity<GameEntity>
{
	public static final int GAME_TYPE_X01 = 1;
	public static final int GAME_TYPE_GOLF = 2;
	public static final int GAME_TYPE_ROUND_THE_CLOCK = 3;
	
	public static final String CLOCK_TYPE_STANDARD = "Standard";
	public static final String CLOCK_TYPE_DOUBLES = "Doubles";
	public static final String CLOCK_TYPE_TREBLES = "Trebles";
	
	private int gameType = -1;
	private String gameParams = "";
	private Timestamp dtFinish = DateUtil.END_OF_TIME;
	private long dartsMatchId = -1;
	private int matchOrdinal = -1;
	
	@Override
	public String getTableName() 
	{
		return "Game";
	}

	@Override
	public String getCreateTableSqlSpecific() 
	{
		return "GameType INT NOT NULL, "
		  + "GameParams varchar(255) NOT NULL, "
		  + "DtFinish timestamp NOT NULL, "
		  + "DartsMatchId INT NOT NULL, "
		  + "MatchOrdinal INT NOT NULL";
	}

	@Override
	public void populateFromResultSet(GameEntity e, ResultSet rs) throws SQLException 
	{
		e.setGameType(rs.getInt("GameType"));
		e.setGameParams(rs.getString("GameParams"));
		e.setDtFinish(rs.getTimestamp("DtFinish"));
		e.setDartsMatchId(rs.getInt("DartsMatchId"));
		e.setMatchOrdinal(rs.getInt("MatchOrdinal"));
	}

	@Override
	public String writeValuesToStatement(PreparedStatement statement, int i, String statementStr) throws SQLException 
	{
		statementStr = writeInt(statement, i++, gameType, statementStr);
		statementStr = writeString(statement, i++, gameParams, statementStr);
		statementStr = writeTimestamp(statement, i++, dtFinish, statementStr);
		statementStr = writeLong(statement, i++, dartsMatchId, statementStr);
		statementStr = writeInt(statement, i++, matchOrdinal, statementStr);
		
		return statementStr;
	}
	
	@Override
	public void addListsOfColumnsForIndexes(ArrayList<ArrayList<String>> indexes)
	{
		ArrayList<String> gameTypeIndex = new ArrayList<>();
		gameTypeIndex.add("GameType");
		indexes.add(gameTypeIndex);
	}
	
	@Override
	protected ArrayList<String> getColumnsAllowedToBeUnset()
	{
		ArrayList<String> ret = new ArrayList<>();
		ret.add("DartsMatchId"); 
		return ret;
	}
	
	@Override
	public long getGameId()
	{
		return getRowId();
	}
	
	public int getParticipantCount()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(1) FROM ");
		sb.append(new ParticipantEntity().getTableName());
		sb.append(" WHERE GameId = ");
		sb.append(getRowId());

		return DatabaseUtil.executeQueryAggregate(sb);
	}
	
	public HandyArrayList<PlayerEntity> retrievePlayersVector()
	{
		HandyArrayList<PlayerEntity> ret = new HandyArrayList<>();
		
		String whereSql = "GameId = " + getRowId() + " ORDER BY Ordinal ASC";
		ArrayList<ParticipantEntity> participants = new ParticipantEntity().retrieveEntities(whereSql);
		
		
		for (int i=0; i<participants.size(); i++)
		{
			ParticipantEntity p = participants.get(i);
			PlayerEntity player = p.getPlayer();
			
			ret.add(player);
		}
		
		return ret;
	}
	
	/**
	 * Ordered by RowId as well because of a bug with loading where the ordinals could get screwed up.
	 */
	public static HandyArrayList<GameEntity> retrieveGamesForMatch(long matchId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DartsMatchId = ");
		sb.append(matchId);
		sb.append(" ORDER BY MatchOrdinal, RowId"); 
		
		return new GameEntity().retrieveEntities(sb.toString());
	}

	/**
	 * Gets / sets
	 */
	public int getGameType()
	{
		return gameType;
	}
	public void setGameType(int gameType)
	{
		this.gameType = gameType;
	}
	public String getGameParams()
	{
		return gameParams;
	}
	public void setGameParams(String gameParams)
	{
		this.gameParams = gameParams;
	}
	public Timestamp getDtFinish()
	{
		return dtFinish;
	}
	public void setDtFinish(Timestamp dtFinish)
	{
		this.dtFinish = dtFinish;
	}
	public long getDartsMatchId()
	{
		return dartsMatchId;
	}
	public void setDartsMatchId(long matchId)
	{
		this.dartsMatchId = matchId;
	}
	public int getMatchOrdinal()
	{
		return matchOrdinal;
	}
	public void setMatchOrdinal(int matchOrdinal)
	{
		this.matchOrdinal = matchOrdinal;
	}

	/**
	 * Helpers
	 */
	public boolean isFinished()
	{
		return !DateUtil.isEndOfTime(dtFinish);
	}
	public String getTypeDesc()
	{
		return getTypeDesc(gameType, gameParams);
	}
	

	/**
	 * Static methods
	 */
	public static GameEntity factoryAndSave(int gameType, String gameParams)
	{
		GameEntity gameEntity = new GameEntity();
		gameEntity.assignRowId();
		gameEntity.setGameType(gameType);
		gameEntity.setGameParams(gameParams);
		gameEntity.saveToDatabase();
		return gameEntity;
	}
	public static GameEntity factoryAndSave(DartsMatchEntity match)
	{
		GameEntity gameEntity = new GameEntity();
		gameEntity.assignRowId();
		gameEntity.setGameType(match.getGameType());
		gameEntity.setGameParams(match.getGameParams());
		gameEntity.setDartsMatchId(match.getRowId());
		gameEntity.setMatchOrdinal(match.incrementAndGetCurrentOrdinal());
		gameEntity.saveToDatabase();
		return gameEntity;
	}
	
	public static String getTypeDesc(int gameType, String gameParams)
	{
		if (gameType == GAME_TYPE_X01)
		{
			return gameParams;
		}
		
		if (gameType == GAME_TYPE_GOLF)
		{
			return "Golf - " + gameParams + " holes";
		}
		
		if (gameType == GAME_TYPE_ROUND_THE_CLOCK)
		{
			return "Round the Clock - " + gameParams;
		}
		
		return "";
	}
	public static String getTypeDesc(int gameType)
	{
		if (gameType == GAME_TYPE_X01)
		{
			return "X01";
		}
		
		if (gameType == GAME_TYPE_GOLF)
		{
			return "Golf";
		}
		
		if (gameType == GAME_TYPE_ROUND_THE_CLOCK)
		{
			return "Round the Clock";
		}
		
		return "<Game Type>";
	}
	
	public static GameParamFilterPanel getFilterPanel(int gameType)
	{
		if (gameType == GAME_TYPE_X01)
		{
			return new GameParamFilterPanelX01();
		}
		
		if (gameType == GAME_TYPE_GOLF)
		{
			return new GameParamFilterPanelGolf();
		}
		
		if (gameType == GAME_TYPE_ROUND_THE_CLOCK)
		{
			return new GameParamFilterPanelRoundTheClock();
		}
		
		return null;
	}
	
	public static HandyArrayList<Integer> getAllGameTypes()
	{
		return ClassUtil.getAllDeclaredFieldValues(GameEntity.class, int.class, "GAME_TYPE_");
	}
}