package code.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import object.HandyArrayList;

/**
 * Entity to record a particular that a particular achievement has been earned by a player.
 * 
 * Points at a GameId if applicable, and uses DtCreation as the "date unlocked".
 */
public final class AchievementEntity extends AbstractEntity<AchievementEntity>
{
	private long playerId = -1;
	private int achievementRef = -1;
	private long gameIdEarned = -1;
	private int achievementCounter = -1;
	
	@Override
	public String getTableName()
	{
		return "Achievement";
	}

	@Override
	public String getCreateTableSqlSpecific()
	{
		return "PlayerId INT NOT NULL, "
		  + "AchievementRef INT NOT NULL, "
		  + "GameIdEarned INT NOT NULL, "
		  + "AchievementCounter INT NOT NULL";
	}

	@Override
	public void populateFromResultSet(AchievementEntity entity, ResultSet rs)
			throws SQLException
	{
		entity.setPlayerId(rs.getInt("PlayerId"));
		entity.setAchievementRef(rs.getInt("AchievementRef"));
		entity.setGameIdEarned(rs.getInt("GameIdEarned"));
		entity.setAchievementCounter(rs.getInt("AchievementCounter"));
	}

	@Override
	public String writeValuesToStatement(PreparedStatement statement, int i, String statementStr) throws SQLException
	{
		statementStr = writeLong(statement, i++, playerId, statementStr);
		statementStr = writeInt(statement, i++, achievementRef, statementStr);
		statementStr = writeLong(statement, i++, gameIdEarned, statementStr);
		statementStr = writeInt(statement, i++, achievementCounter, statementStr);
		
		return statementStr;
	}
	
	@Override
	public void addListsOfColumnsForIndexes(ArrayList<ArrayList<String>> indexes)
	{
		ArrayList<String> ix = HandyArrayList.factoryAdd("PlayerId");
		ArrayList<String> ix2 = HandyArrayList.factoryAdd("AchievementRef");
		
		indexes.add(ix);
		indexes.add(ix2);
	}

	/**
	 * Gets / Sets
	 */
	public long getPlayerId()
	{
		return playerId;
	}
	public void setPlayerId(long playerId)
	{
		this.playerId = playerId;
	}
	public int getAchievementRef()
	{
		return achievementRef;
	}
	public void setAchievementRef(int achievementId)
	{
		this.achievementRef = achievementId;
	}
	public long getGameIdEarned()
	{
		return gameIdEarned;
	}
	public void setGameIdEarned(long gameIdEarned)
	{
		this.gameIdEarned = gameIdEarned;
	}
	public int getAchievementCounter()
	{
		return achievementCounter;
	}
	public void setAchievementCounter(int achievementCounter)
	{
		this.achievementCounter = achievementCounter;
	}
}
