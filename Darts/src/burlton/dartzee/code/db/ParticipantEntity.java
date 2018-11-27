package burlton.dartzee.code.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import burlton.dartzee.code.ai.AbstractDartsModel;
import burlton.desktopcore.code.util.DateUtil;
import burlton.core.code.util.StringUtil;

/**
 * Represents the participant of a game. This is a link between a player and a game, with additional information
 * such as play position and finishing position.
 */
public class ParticipantEntity extends AbstractDartsEntity<ParticipantEntity>
{
	private long gameId = -1;
	private long playerId = -1;
	private int ordinal = -1;
	private int finishingPosition = -1;
	private int finalScore = -1;
	private Timestamp dtFinished = DateUtil.END_OF_TIME;
	
	//In memory things
	private PlayerEntity player = null;

	@Override
	public String getTableName()
	{
		return "Participant";
	}

	@Override
	public String getCreateTableSqlSpecific()
	{
		return "GameId INT NOT NULL, "
				+ "PlayerId INT NOT NULL, "
				+ "Ordinal INT NOT NULL, "
				+ "FinishingPosition INT NOT NULL, "
				+ "FinalScore INT NOT NULL, "
				+ "DtFinished TIMESTAMP NOT NULL";
	}

	@Override
	public void populateFromResultSet(ParticipantEntity pt, ResultSet rs) throws SQLException
	{
		pt.setGameId(rs.getInt("GameId"));
		pt.setPlayerId(rs.getInt("PlayerId"));
		pt.setOrdinal(rs.getInt("Ordinal"));
		pt.setFinishingPosition(rs.getInt("FinishingPosition"));
		pt.setFinalScore(rs.getInt("FinalScore"));
		pt.setDtFinished(rs.getTimestamp("DtFinished"));
	}

	@Override
	public String writeValuesToStatement(PreparedStatement statement, int i, String statementStr) throws SQLException
	{
		statementStr = writeLong(statement, i++, gameId, statementStr);
		statementStr = writeLong(statement, i++, playerId, statementStr);
		statementStr = writeInt(statement, i++, ordinal, statementStr);
		statementStr = writeInt(statement, i++, finishingPosition, statementStr);
		statementStr = writeInt(statement, i++, finalScore, statementStr);
		statementStr = writeTimestamp(statement, i++, dtFinished, statementStr);
		
		return statementStr;
	}
	
	public static ParticipantEntity factoryAndSave(long gameId, PlayerEntity player, int ordinal)
	{
		ParticipantEntity gp = new ParticipantEntity();
		gp.assignRowId();
		gp.setGameId(gameId);
		gp.setPlayerId(player.getRowId());
		gp.setOrdinal(ordinal);
		
		//Cache the actual player entity so we can access its strategy etc
		gp.setPlayer(player);
		
		gp.saveToDatabase();
		return gp;
	}
	
	@Override
	public void addListsOfColumnsForIndexes(ArrayList<ArrayList<String>> indexes)
	{
		ArrayList<String> playerId_gameId = new ArrayList<>();
		playerId_gameId.add("PlayerId");
		playerId_gameId.add("GameId");
		indexes.add(playerId_gameId);
	}
	
	/**
	 * Helpers
	 */
	public boolean isAi()
	{
		return player.isAi();
	}
	public boolean isActive()
	{
		return DateUtil.isEndOfTime(dtFinished);
	}
	public String getFinishingPositionDesc()
	{
		return StringUtil.convertOrdinalToText(finishingPosition);
	}
	

	@Override
	public String toString()
	{
		return "Player " + ordinal + " in Game #" + gameId + " [PlayerId " + playerId + "]";
	}
	
	/**
	 * Gets / Sets
	 */
	@Override
	public long getGameId()
	{
		return gameId;
	}
	public void setGameId(long gameId)
	{
		this.gameId = gameId;
	}
	public long getPlayerId()
	{
		return playerId;
	}
	public void setPlayerId(long playerId)
	{
		this.playerId = playerId;
	}
	public int getOrdinal()
	{
		return ordinal;
	}
	public void setOrdinal(int ordinal)
	{
		this.ordinal = ordinal;
	}
	public int getFinishingPosition()
	{
		return finishingPosition;
	}
	public void setFinishingPosition(int finishingPosition)
	{
		this.finishingPosition = finishingPosition;
	}
	public int getFinalScore()
	{
		return finalScore;
	}
	public void setFinalScore(int finalScore)
	{
		this.finalScore = finalScore;
	}
	public Timestamp getDtFinished()
	{
		return dtFinished;
	}
	public void setDtFinished(Timestamp dtFinished)
	{
		this.dtFinished = dtFinished;
	}

	/**
	 * Non-db Gets / Sets
	 */
	public PlayerEntity getPlayer()
	{
		if (player == null)
		{
			player = new PlayerEntity().retrieveForId(playerId);
		}
		
		return player;
	}
	public void setPlayer(PlayerEntity player)
	{
		this.player = player;
	}
	public AbstractDartsModel getModel()
	{
		return player.getModel();
	}
	public String getPlayerName()
	{
		return getPlayer().getName();
	}
}
