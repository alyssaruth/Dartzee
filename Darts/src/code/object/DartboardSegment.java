package code.object;

import java.awt.Point;
import java.util.ArrayList;

import object.HashMapList;
import util.StringUtil;

public class DartboardSegment 
{
	public static final int TYPE_DOUBLE = 1;
	public static final int TYPE_TREBLE = 2;
	public static final int TYPE_OUTER_SINGLE = 3;
	public static final int TYPE_INNER_SINGLE = 4;
	public static final int TYPE_MISS = 5;
	public static final int TYPE_MISSED_BOARD = 6;
	
	//The Points this segment contains
	private ArrayList<Point> points = new ArrayList<>();
	private HashMapList<Integer, Point> hmXCoordToPoints = new HashMapList<>();
	private HashMapList<Integer, Point> hmYCoordToPoints = new HashMapList<>();
	
	private int score = -1;
	private int type = -1;
	
	public DartboardSegment(String scoreAndType)
	{
		ArrayList<String> toks = StringUtil.getListFromDelims(scoreAndType, "_");
		
		this.score = Integer.parseInt(toks.get(0));
		this.type = Integer.parseInt(toks.get(1));
	}
	
	public void addPoint(Point pt)
	{
		points.add(pt);
		
		int x = (int)pt.getX();
		hmXCoordToPoints.putInList(x, pt);
		
		int y = (int)pt.getY();
		hmYCoordToPoints.putInList(y, pt);
	}
	public ArrayList<Point> getPoints()
	{
		return points;
	}
	
	@Override
	public String toString() 
	{
		return score + " (" + type + ")";
	}
	
	/**
	 * Helpers
	 */
	public boolean isMiss()
	{
		return type == TYPE_MISS
		  || type == TYPE_MISSED_BOARD;
	}
	public boolean isDoubleExcludingBull()
	{
		return type == TYPE_DOUBLE
		  && score != 25;
	}
	public int getMultiplier()
	{
		return getMultiplier(type);
	}
	public boolean isEdgePoint(Point pt)
	{
		boolean canBeYMax = true;
		boolean canBeYMin = true;
		boolean canBeXMax = true;
		boolean canBeXMin = true;
		
		int x = (int)pt.getX();
		ArrayList<Point> otherXPts = hmXCoordToPoints.get(x);
		for (Point otherPt : otherXPts)
		{
			if (otherPt.getY() < pt.getY())
			{
				canBeYMin = false;
			}
			
			if (otherPt.getY() > pt.getY())
			{
				canBeYMax = false;
			}
		}
		
		int y = (int)pt.getY();
		ArrayList<Point> otherYPts = hmYCoordToPoints.get(y);
		for (Point otherPt : otherYPts)
		{
			if (otherPt.getX() < pt.getX())
			{
				canBeXMin = false;
			}
			
			if (otherPt.getX() > pt.getX())
			{
				canBeXMax = false;
			}
		}
		
		return canBeYMax || canBeYMin || canBeXMax || canBeXMin;
	}
	public static int getGolfScore(int type)
	{
		if (type == TYPE_DOUBLE)
		{
			return 1;
		}
		else if (type == TYPE_TREBLE)
		{
			return 2;
		}
		else if (type == TYPE_INNER_SINGLE)
		{
			return 3;
		}
		else if (type == TYPE_OUTER_SINGLE)
		{
			return 4;
		}
		
		return 5;
	}
	
	/**
	 * Static methods
	 */
	public static int getMultiplier(int type)
	{
		if (type == TYPE_DOUBLE)
		{
			return 2;
		}
		else if (type == TYPE_TREBLE)
		{
			return 3;
		}
		else if (type == TYPE_MISS
		  || type == TYPE_MISSED_BOARD)
		{
			return 0;
		}
		
		return 1;
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
	public int getType() 
	{
		return type;
	}
	public void setType(int type) 
	{
		this.type = type;
	}
}
