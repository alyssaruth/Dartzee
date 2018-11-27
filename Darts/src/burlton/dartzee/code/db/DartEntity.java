package burlton.dartzee.code.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import burlton.dartzee.code.object.Dart;

public class DartEntity extends AbstractDartsEntity<DartEntity>
{
	private long roundId = -1;
	private int ordinal = -1;
	private int score = -1;
	private int multiplier = -1;
	private int startingScore = -1;
	private int posX = -1;
	private int posY = -1;
	private int segmentType = -1;
	
	@Override
	public String getTableName() 
	{
		return "Dart";
	}

	@Override
	public String getCreateTableSqlSpecific() 
	{
		return "RoundId INT NOT NULL, "
		  + "Ordinal INT NOT NULL, "
		  + "Score INT NOT NULL, "
		  + "Multiplier INT NOT NULL, "
		  + "StartingScore INT NOT NULL, "
		  + "PosX INT NOT NULL, "
		  + "PosY INT NOT NULL, "
		  + "SegmentType INT NOT NULL";
	}

	@Override
	public void populateFromResultSet(DartEntity dart, ResultSet rs) throws SQLException 
	{
		dart.setRoundId(rs.getLong("RoundId"));
		dart.setOrdinal(rs.getInt("Ordinal"));
		dart.setScore(rs.getInt("Score"));
		dart.setMultiplier(rs.getInt("Multiplier"));
		dart.setStartingScore(rs.getInt("StartingScore"));
		dart.setPosX(rs.getInt("PosX"));
		dart.setPosY(rs.getInt("PosY"));
		dart.setSegmentType(rs.getInt("SegmentType"));
	}

	@Override
	public String writeValuesToStatement(PreparedStatement statement, int i, String statementStr) throws SQLException 
	{
		statementStr = writeLong(statement, i++, roundId, statementStr);
		statementStr = writeInt(statement, i++, ordinal, statementStr);
		statementStr = writeInt(statement, i++, score, statementStr);
		statementStr = writeInt(statement, i++, multiplier, statementStr);
		statementStr = writeInt(statement, i++, startingScore, statementStr);
		statementStr = writeInt(statement, i++, posX, statementStr);
		statementStr = writeInt(statement, i++, posY, statementStr);
		statementStr = writeInt(statement, i++, segmentType, statementStr);
		
		return statementStr;
	}
	
	@Override
	public long getGameId()
	{
		return retrieveRound().retrieveParticipant().getGameId();
	}
	public RoundEntity retrieveRound()
	{
		return new RoundEntity().retrieveForId(roundId);
	}
	
	public static DartEntity factoryAndSave(Dart dart, long roundId, int ordinal, int startingScore)
	{
		DartEntity de = new DartEntity();
		de.assignRowId();
		de.setScore(dart.getScore());
		de.setMultiplier(dart.getMultiplier());
		de.setRoundId(roundId);
		de.setOrdinal(ordinal);
		de.setStartingScore(startingScore);
		de.setPosX(dart.getX());
		de.setPosY(dart.getY());
		de.setSegmentType(dart.getSegmentType());
		
		de.saveToDatabase();
		return de;
	}
	
	@Override
	public void addListsOfColumnsForIndexes(ArrayList<ArrayList<String>> indexes)
	{
		ArrayList<String> roundIdIndex = new ArrayList<>();
		roundIdIndex.add("RoundId");
		roundIdIndex.add("Ordinal");
		indexes.add(roundIdIndex);
	}

	/**
	 * Gets / sets
	 */
	public long getRoundId()
	{
		return roundId;
	}
	public void setRoundId(long roundId)
	{
		this.roundId = roundId;
	}
	public int getOrdinal()
	{
		return ordinal;
	}
	public void setOrdinal(int ordinal)
	{
		this.ordinal = ordinal;
	}
	public int getScore()
	{
		return score;
	}
	public void setScore(int score)
	{
		this.score = score;
	}
	public int getMultiplier()
	{
		return multiplier;
	}
	public void setMultiplier(int multiplier)
	{
		this.multiplier = multiplier;
	}
	public int getStartingScore()
	{
		return startingScore;
	}
	public void setStartingScore(int startingScore)
	{
		this.startingScore = startingScore;
	}
	public int getPosX()
	{
		return posX;
	}
	public void setPosX(int posX)
	{
		this.posX = posX;
	}
	public int getPosY()
	{
		return posY;
	}
	public void setPosY(int posY)
	{
		this.posY = posY;
	}
	public int getSegmentType()
	{
		return segmentType;
	}
	public void setSegmentType(int segmentType)
	{
		this.segmentType = segmentType;
	}
}
