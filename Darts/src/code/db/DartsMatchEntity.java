package code.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import code.utils.DatabaseUtil;
import object.HandyArrayList;
import util.DateUtil;
import util.Debug;
import util.XmlUtil;

/**
 * Simple entity to join multiple 'games' together into a 'match'.
 * Table has to be called 'DartsMatch' because 'Match' is a derby keyword!
 */
public class DartsMatchEntity extends AbstractDartsEntity<DartsMatchEntity>
{
	public static final int MODE_FIRST_TO = 0;
	public static final int MODE_POINTS = 1;
	
	private int games = -1;
	private int mode = -1;
	private Timestamp dtFinish = DateUtil.END_OF_TIME;
	private String matchParams = "";
	
	//Non-db things
	private int currentOrdinal = 0;
	private String gameParams = "";
	private int gameType = -1;
	private HandyArrayList<PlayerEntity> players = new HandyArrayList<>();
	private HashMap<Integer, Integer> hmPositionToPoints = null;
	
	@Override
	public String getTableName()
	{
		return "DartsMatch";
	}

	@Override
	public String getCreateTableSqlSpecific()
	{
		return "Games INT NOT NULL, Mode INT NOT NULL, DtFinish TIMESTAMP NOT NULL, MatchParams VARCHAR(255) NOT NULL";
	}

	@Override
	public void populateFromResultSet(DartsMatchEntity entity, ResultSet rs)
			throws SQLException
	{
		entity.setGames(rs.getInt("Games"));
		entity.setMode(rs.getInt("Mode"));
		entity.setDtFinish(rs.getTimestamp("DtFinish"));
		entity.setMatchParams(rs.getString("MatchParams"));
	}

	@Override
	public String writeValuesToStatement(PreparedStatement statement, int i, String statementStr) throws SQLException
	{
		statementStr = writeInt(statement, i++, games, statementStr);
		statementStr = writeInt(statement, i++, mode, statementStr);
		statementStr = writeTimestamp(statement, i++, dtFinish, statementStr);
		statementStr = writeString(statement, i++, matchParams, statementStr);
		return statementStr;
	}
	
	@Override
	public long getGameId()
	{
		return -1;
	}
	
	/**
	 * Helpers
	 */
	public int getScoreForFinishingPosition(int position)
	{
		if (mode == MODE_FIRST_TO)
		{
			return position == 1? 1:0;
		}
		
		if (mode == MODE_POINTS)
		{
			if (position == -1)
			{
				return 0;
			}
			
			return getHmPositionToPoints().get(position);
		}
		
		Debug.stackTrace("Unimplemented for match mode [" + mode + "]");
		return -1;
	}
	public boolean isComplete()
	{
		if (mode == MODE_FIRST_TO)
		{
			return isFirstToMatchComplete();
		}
		else if (mode == MODE_POINTS)
		{
			return isPointsMatchComplete();
		}
		
		Debug.stackTrace("Unimplemented for match mode [" + mode + "]");
		return false;
	}
	private boolean isFirstToMatchComplete()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT COUNT(1) AS WinCount");
		sb.append(" FROM Participant p, Game g");
		sb.append(" WHERE g.DartsMatchId = ");
		sb.append(getRowId());
		sb.append(" AND p.GameId = g.RowId");
		sb.append(" AND p.FinishingPosition = 1");
		sb.append(" GROUP BY p.PlayerId");
		sb.append(" ORDER BY COUNT(1) DESC");
		sb.append(" FETCH FIRST 1 ROWS ONLY");
		
		try (ResultSet rs = DatabaseUtil.executeQuery(sb))
		{
			if (rs.next())
			{
				int count = rs.getInt("WinCount");
				return count == games;
			}
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(sb.toString(), sqle);
		}
		
		return false;
	}
	private boolean isPointsMatchComplete()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT COUNT(1)");
		sb.append(" FROM Game");
		sb.append(" WHERE DartsMatchId = ");
		sb.append(getRowId());
		sb.append(" AND DtFinish < ");
		sb.append(DateUtil.getEndOfTimeSqlString());
		
		long count = DatabaseUtil.executeQueryAggregate(sb);
		return count == games;
	}
	public int incrementAndGetCurrentOrdinal()
	{
		return currentOrdinal++;
	}
	
	public int getPlayerCount()
	{
		return players.size();
	}
	
	public void shufflePlayers()
	{
		if (players.size() == 2)
		{
			players = players.reverse();
		}
		else
		{
			
			Collections.shuffle(players);
		}
	}
	
	public String getMatchDesc()
	{
		return "Match #" + getRowId() 
			+ " (" + getMatchTypeDesc() + " - " 
			+ GameEntity.getTypeDesc(gameType, gameParams) + ", " 
			+ getPlayerCount() + " players)";
	}
	private String getMatchTypeDesc()
	{
		if (mode == MODE_FIRST_TO)
		{
			return "First to " + games;
		}
		
		if (mode == MODE_POINTS)
		{
			return "Points based (" + games + " games)";
		}
		
		return "";
		
	}
	
	private HashMap<Integer, Integer> getHmPositionToPoints()
	{
		if (hmPositionToPoints == null)
		{
			hmPositionToPoints = new HashMap<>();
			
			Document doc = XmlUtil.getDocumentFromXmlString(matchParams);
			Element root = doc.getDocumentElement();
			
			hmPositionToPoints.put(1, XmlUtil.getAttributeInt(root, "First"));
			hmPositionToPoints.put(2, XmlUtil.getAttributeInt(root, "Second"));
			hmPositionToPoints.put(3, XmlUtil.getAttributeInt(root, "Third"));
			hmPositionToPoints.put(4, XmlUtil.getAttributeInt(root, "Fourth"));
		}
		
		return hmPositionToPoints;
	}
	
	public void cacheMetadataFromGame(GameEntity game)
	{
		this.gameType = game.getGameType();
		this.gameParams = game.getGameParams();
		this.players = game.retrievePlayersVector();
		
		//Should've been setting this too...
		this.currentOrdinal = game.getMatchOrdinal();
	}
	
	/**
	 * Factory methods
	 */
	public static DartsMatchEntity factoryFirstTo(int games)
	{
		return factoryAndSave(games, MODE_FIRST_TO, "");
	}
	public static DartsMatchEntity factoryPoints(int games, String pointsXml)
	{
		return factoryAndSave(games, MODE_POINTS, pointsXml);
	}
	private static DartsMatchEntity factoryAndSave(int games, int mode, String matchParams)
	{
		DartsMatchEntity matchEntity = new DartsMatchEntity();
		matchEntity.assignRowId();
		matchEntity.setMode(mode);
		matchEntity.setGames(games);
		matchEntity.setMatchParams(matchParams);
		matchEntity.saveToDatabase();
		return matchEntity;
	}

	/**
	 * Gets / Sets
	 */
	public int getGames()
	{
		return games;
	}
	public void setGames(int games)
	{
		this.games = games;
	}
	public int getMode()
	{
		return mode;
	}
	public void setMode(int mode)
	{
		this.mode = mode;
	}
	public Timestamp getDtFinish()
	{
		return dtFinish;
	}
	public void setDtFinish(Timestamp dtFinish)
	{
		this.dtFinish = dtFinish;
	}
	public String getMatchParams()
	{
		return matchParams;
	}
	public void setMatchParams(String matchParams)
	{
		this.matchParams = matchParams;
	}

	/**
	 * Non-db gets / sets
	 */
	public String getGameParams()
	{
		return gameParams;
	}
	public void setGameParams(String gameParams)
	{
		this.gameParams = gameParams;
	}
	public int getGameType()
	{
		return gameType;
	}
	public void setGameType(int gameType)
	{
		this.gameType = gameType;
	}
	public HandyArrayList<PlayerEntity> getPlayers()
	{
		return players;
	}
	public void setPlayers(HandyArrayList<PlayerEntity> players)
	{
		this.players = players;
	}
}
