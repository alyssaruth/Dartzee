package burlton.dartzee.code.db;

import burlton.dartzee.code.object.Dart;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DartEntity extends AbstractEntity<DartEntity>
{
	private String roundId = "";
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
		return "RoundId VARCHAR(36) NOT NULL, "
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
		dart.setRoundId(rs.getString("RoundId"));
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
		statementStr = writeString(statement, i++, roundId, statementStr);
		statementStr = writeInt(statement, i++, ordinal, statementStr);
		statementStr = writeInt(statement, i++, score, statementStr);
		statementStr = writeInt(statement, i++, multiplier, statementStr);
		statementStr = writeInt(statement, i++, startingScore, statementStr);
		statementStr = writeInt(statement, i++, posX, statementStr);
		statementStr = writeInt(statement, i++, posY, statementStr);
		statementStr = writeInt(statement, i++, segmentType, statementStr);
		
		return statementStr;
	}
	
	public static DartEntity factoryAndSave(Dart dart, String roundId, int ordinal, int startingScore)
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
	public void addListsOfColumnsForIndexes(List<List<String>> indexes)
	{
		ArrayList<String> roundIdIndex = new ArrayList<>();
		roundIdIndex.add("RoundId");
		roundIdIndex.add("Ordinal");
		indexes.add(roundIdIndex);
	}

	/**
	 * Gets / sets
	 */
	public String getRoundId()
	{
		return roundId;
	}
	public void setRoundId(String roundId)
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
