package burlton.dartzee.code.object;

import java.awt.Point;
import java.util.ArrayList;

import burlton.dartzee.code.db.GameEntity;
import burlton.dartzee.code.utils.DartsRegistry;
import burlton.dartzee.code.utils.PreferenceUtil;
import burlton.core.code.util.StringUtil;

public class Dart implements DartsRegistry
{
	private Point pt = null;
	private int score = -1;
	private int multiplier = -1; //double or triple
	private int segmentType = -1; //Outer single, miss, etc
	
	private int ordinal = -1;
	
	//What the player's "score" was before throwing this dart.
	//Means what you'd think for X01
	//For Round the Clock, this'll be what they were to aim for
	private int startingScore = 1; 
	
	//Never set on the DB. Used for in-game stats, and is just set to the round number.
	private int golfHole = -1;
	private long participantId = -1;
	
	public Dart(int score, int multiplier)
	{
		this.score = score;
		this.multiplier = multiplier;
	}
	public Dart(Point pt, int score, int multiplier, int segmentType)
	{
		this.pt = pt;
		this.score = score;
		this.multiplier = multiplier;
		this.segmentType = segmentType;
	}
	
	/**
	 * Helpers
	 */
	public boolean isDouble()
	{
		return multiplier == 2;
	}
	public boolean isTriple()
	{
		return multiplier == 3;
	}
	public int getTotal()
	{
		return score * multiplier;
	}
	public int getGolfScore(int target)
	{
		if (score != target)
		{
			return 5;
		}
		
		return DartboardSegment.getGolfScore(segmentType);
	}
	
	public int getGolfScore()
	{
		return getGolfScore(golfHole);
	}
	
	
	/**
	 * Gets / sets
	 */
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
	public int getOrdinal()
	{
		return ordinal;
	}
	public void setOrdinal(int ordinal)
	{
		this.ordinal = ordinal;
	}
	public int getX()
	{
		return (int)pt.getX();
	}
	public int getY()
	{
		return (int)pt.getY();
	}
	public int getSegmentType()
	{
		return segmentType;
	}
	public void setSegmentType(int segmentType)
	{
		this.segmentType = segmentType;
	}
	public int getStartingScore()
	{
		return startingScore;
	}
	public void setStartingScore(int clockNumber)
	{
		this.startingScore = clockNumber;
	}
	public void setGolfHole(int hole)
	{
		this.golfHole = hole;
	}
	public long getParticipantId()
	{
		return participantId;
	}
	public void setParticipantId(long participantId)
	{
		this.participantId = participantId;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + multiplier;
		result = prime * result + ordinal;
		result = prime * result + score;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Dart))
			return false;
		Dart other = (Dart) obj;
		if (multiplier != other.multiplier)
			return false;
		if (ordinal != other.ordinal)
			return false;
		if (score != other.score)
			return false;
		return true;
	}

	@Override
	public String toString() 
	{
		boolean showTotal = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_DISPLAY_DART_TOTAL_SCORE);
		if (showTotal)
		{
			return "" + getTotal();
		}
		
		return getRendered();
	}
	
	public boolean hitClockTarget(String clockType)
	{
		if (clockType.equals(GameEntity.CLOCK_TYPE_DOUBLES)
		  && !isDouble())
		{
			return false;
		}
		
		if (clockType.equals(GameEntity.CLOCK_TYPE_TREBLES)
		  && !isTriple())
		{
			return false;
		}
		
		return score == startingScore
		  && multiplier > 0;
	}
	
	public String getRendered()
	{
		if (multiplier == 0)
		{
			return "0";
		}
		
		String ret = "";
		if (isDouble())
		{
			ret += "D";
		}
		else if (isTriple())
		{
			ret += "T";
		}
		
		ret += score;
		
		return ret;
	}
	
	public int getSegmentTypeToAimAt()
	{
		if (multiplier == 1)
		{
			return DartboardSegment.TYPE_OUTER_SINGLE;
		}
		else if (multiplier == 2)
		{
			return DartboardSegment.TYPE_DOUBLE;
		}
		
		return DartboardSegment.TYPE_TREBLE;
	}
	
	/**
	 * Static methods
	 */
	public static Dart factorySingle(int score)
	{
		return new Dart(score, 1);
	}
	public static Dart factoryDouble(int score)
	{
		return new Dart(score, 2);
	}
	public static Dart factoryTreble(int score)
	{
		return new Dart(score, 3);
	}
	
	public int compareTo(Dart other)
	{
		//If there's a strict inequality in total, then it's simple
		if (getTotal() > other.getTotal())
		{
			return 1;
		}
		
		if (getTotal() < other.getTotal())
		{
			return -1;
		}
		
		//Totals are equal. So now look at the multiplier.
		//I.e. T12 > D18 even though the totals are both 36.
		if (getMultiplier() > other.getMultiplier())
		{
			return 1;
		}
		
		if (other.getMultiplier() > getMultiplier())
		{
			return -1;
		}
		
		//Same total and same multiplier, so must be an equivalent dart
		return 0;
	}
}
