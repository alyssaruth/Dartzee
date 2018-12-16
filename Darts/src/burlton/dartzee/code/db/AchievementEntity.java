package burlton.dartzee.code.db;

import burlton.core.code.obj.HandyArrayList;
import burlton.dartzee.code.achievements.AbstractAchievement;
import burlton.dartzee.code.achievements.AchievementUtilKt;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.dartzee.code.screen.game.DartsGameScreen;
import burlton.desktopcore.code.util.DateUtil;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

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
		ArrayList<String> ix = HandyArrayList.factoryAdd("PlayerId", "AchievementRef");
		
		indexes.add(ix);
	}
	
	public static AchievementEntity retrieveAchievement(int achievementRef, long playerId)
	{
		return new AchievementEntity().retrieveEntity("PlayerId = " + playerId + " AND AchievementRef = " + achievementRef);
	}

	/**
	 *  Methods for gameplay logic to update achievements
	 */
	public static void updateAchievement(int achievementRef, long playerId, long gameId, int counter)
	{
		AchievementEntity existingAchievement = retrieveAchievement(achievementRef, playerId);
	
		if (existingAchievement == null)
		{
			AchievementEntity.factoryAndSave(achievementRef, playerId, gameId, counter);

			triggerAchievementUnlock(-1, counter, achievementRef, playerId, gameId);
		}
		else
		{
			int existingCounter = existingAchievement.getAchievementCounter();

			boolean decreasing = AchievementUtilKt.getAchievementForRef(achievementRef).isDecreasing();

			//Update the achievement if appropriate
			if ((counter > existingCounter && !decreasing)
			 || (counter < existingCounter && decreasing))
			{
				existingAchievement.setAchievementCounter(counter);
				existingAchievement.setGameIdEarned(gameId);
				existingAchievement.saveToDatabase();

				triggerAchievementUnlock(existingCounter, counter, achievementRef, playerId, gameId);
			}
		}
	}
	public static void incrementAchievement(int achievementRef, long playerId, long gameId, int amountBy)
	{
		AchievementEntity existingAchievement = retrieveAchievement(achievementRef, playerId);

		if (existingAchievement == null)
		{
			AchievementEntity.factoryAndSave(achievementRef, playerId, -1, amountBy);

			triggerAchievementUnlock(-1, amountBy, achievementRef, playerId, gameId);
		}
		else
		{
			int existingCount = existingAchievement.getAchievementCounter();
			existingAchievement.setAchievementCounter(existingCount + amountBy);
			existingAchievement.saveToDatabase();

			triggerAchievementUnlock(existingCount, existingCount + amountBy, achievementRef, playerId, gameId);
		}
	}
	private static void triggerAchievementUnlock(int oldValue, int newValue, int achievementRef, long playerId, long gameId)
	{
		AbstractAchievement achievementTemplate = AchievementUtilKt.getAchievementForRef(achievementRef);
		triggerAchievementUnlock(oldValue, newValue, achievementTemplate, playerId, gameId);
	}
	public static void triggerAchievementUnlock(int oldValue, int newValue, AbstractAchievement achievementTemplate, long playerId, long gameId)
	{
		//Work out if the threshold has changed
		achievementTemplate.setAttainedValue(oldValue);

		Color oldColor = achievementTemplate.getColor(false);

		achievementTemplate.setAttainedValue(newValue);
		Color newColor = achievementTemplate.getColor(false);

		if (oldColor != newColor)
		{
			//Hooray we've done a thing!
			DartsGameScreen scrn = ScreenCache.getDartsGameScreen(gameId);
			scrn.achievementUnlocked(gameId, playerId, achievementTemplate);
		}
	}
	
	public static AchievementEntity factoryAndSave(int achievementRef, long playerId, long gameId, int counter)
	{
		return factoryAndSave(achievementRef, playerId, gameId, counter, DateUtil.getSqlDateNow());
	}
	public static AchievementEntity factoryAndSave(int achievementRef, long playerId, long gameId, int counter,
	  Timestamp dtLastUpdate)
	{
		AchievementEntity ae = new AchievementEntity();
		ae.assignRowId();
		ae.achievementRef = achievementRef;
		ae.playerId = playerId;
		ae.gameIdEarned = gameId;
		ae.achievementCounter = counter;
		ae.saveToDatabase(dtLastUpdate);
		
		return ae;
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
