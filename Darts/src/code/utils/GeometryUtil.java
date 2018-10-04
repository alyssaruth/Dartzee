package code.utils;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import code.object.Quadrant;
import util.Debug;

public abstract class GeometryUtil
{
	private static final Quadrant TOP_RIGHT = Quadrant.factory(0, 90, true, false);
	private static final Quadrant BOTTOM_RIGHT = Quadrant.factory(90, 180, true, true);
	private static final Quadrant BOTTOM_LEFT = Quadrant.factory(180, 270, false, true);
	private static final Quadrant TOP_LEFT = Quadrant.factory(270, 360, false, false);
	
	private static final Quadrant[] QUADRANTS = {TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_LEFT};
	
	public static Point translatePoint(Point pt, double radius, double degrees, boolean logging)
	{
		Debug.appendBanner("Translating " + pt + " by " + radius + " at angle " + degrees, logging);
		
		Quadrant quadrant = getQuadrantForAngle(degrees);
		if (quadrant == null)
		{
			return translatePointAlongAxis(pt, radius, degrees, logging);
		}
		
		//Need radians for trig functions
		double theta = Math.toRadians(degrees);
		double dSin = Math.abs(radius * Math.sin(theta));
		double dCos = Math.abs(radius * Math.cos(theta));
		
		Debug.appendWithoutDate("dSin = " + dSin + ", dCos = " + dCos, logging);
		
		double x = dSin;
		double y = dCos;
		/*if (quadrant.isSinForX())
		{
			x = dSin;
			y = dCos;
		}*/
		
		if (!quadrant.getXIsPositive())
		{
			x *= -1;
		}
		
		if (!quadrant.getYIsPositive())
		{
			y *= -1;
		}
		
		Debug.appendWithoutDate("Translating x: " + x, logging);
		Debug.appendWithoutDate("Translating y: " + y, logging);
		
		x += pt.getX();
		y += pt.getY();
		
		Point ret = new Point();
		ret.setLocation(x, y);
		
		Debug.appendWithoutDate("New point: " + ret, logging);
		return ret;
	}
	
	private static Point translatePointAlongAxis(Point pt, double radius, double degrees, boolean logging)
	{
		Debug.appendWithoutDate("Translating along axis", logging);
		
		double x = pt.getX();
		double y = pt.getY();
		
		Point ret = new Point();
		
		if (degrees == 0)
		{
			ret.setLocation(x, y - radius);
		}
		else if (degrees == 90)
		{
			ret.setLocation(x + radius, y);
		}
		else if (degrees == 180)
		{
			ret.setLocation(x, y + radius);
		}
		else if (degrees == 270)
		{
			ret.setLocation(x - radius, y);
		}
		
		Debug.appendWithoutDate("New point: " + ret, logging);
		return ret;
	}
	
	public static double getDistance(Point dartPt, Point centerPt)
	{
		double xLength = Math.abs(dartPt.getX() - centerPt.getX());
		double yLength = Math.abs(dartPt.getY() - centerPt.getY());
		return Math.sqrt((xLength*xLength) + (yLength*yLength));
	}
	
	/**
	 * Compute the clockwise angle for the point, relative to the center
	 */
	public static double getAngleForPoint(Point dartPt, Point centerPt)
	{
		double xLength = dartPt.getX() - centerPt.getX();
		double yLength = dartPt.getY() - centerPt.getY();
		double hypotenuse = Math.sqrt((xLength*xLength) + (yLength*yLength));
		
		double angle = -1;
		if (xLength == 0)
		{
			angle = yLength > 0 ? 180:0;
		}
		else if (yLength == 0)
		{
			angle = xLength > 0 ? 90:270;
		}
		else
		{
			//We're not on an axis
			boolean xIsPositive = xLength > 0;
			boolean yIsPositive = yLength > 0;

			Quadrant quadrant = getQuadrant(xIsPositive, yIsPositive);
			int angleToAdd = quadrant.getMinimumAngle();
			
			double lengthForCalculation = Math.abs(xLength);
			if (quadrant.isSinForX())
			{
				lengthForCalculation = Math.abs(yLength);
			}
			
			double arcCosValue = Math.acos(lengthForCalculation/hypotenuse);
			arcCosValue = Math.abs(Math.toDegrees(arcCosValue));
			
			angle = angleToAdd + arcCosValue;
		}
		
		return angle;
	}
	
	/**
	 * For the given angle, return the Quadrant. Returns null if there is none (because we're on an axis).
	 */
	private static Quadrant getQuadrantForAngle(double angle)
	{
		for (int i=0; i<QUADRANTS.length; i++)
		{
			Quadrant quadrant = QUADRANTS[i];
			int minAngle = quadrant.getMinimumAngle();
			int maxAngle = quadrant.getMaximumAngle();
			
			if (minAngle < angle 
			  && angle < maxAngle)
			{
				return quadrant;
			}
		}
		
		return null;
	}
	
	private static Quadrant getQuadrant(boolean xIsPositive, boolean yIsPositive)
	{
		for (int i=0; i<QUADRANTS.length; i++)
		{
			Quadrant quadrant = QUADRANTS[i];
			if (quadrant.getXIsPositive() == xIsPositive
			  && quadrant.getYIsPositive() == yIsPositive)
			{
				return quadrant;
			}
		}
		
		Debug.stackTrace("Impossible situation trying to get quadrant. XIsPositive: " + xIsPositive + " yIsPositive: " + yIsPositive);
		return null;
	}
	
	/**
	 * For a group of points, calculate the average point
	 */
	public static Point getAverage(ArrayList<Point> points)
	{
		int size = points.size();
		double xAvg = 0;
		double yAvg = 0;
		
		for (int i=0; i<size; i++)
		{
			Point point = points.get(i);
			xAvg += point.getX();
			yAvg += point.getY();
		}
		
		xAvg = xAvg/size;
		yAvg = yAvg/size;
		
		Point ret = new Point();
		ret.setLocation(xAvg, yAvg);
		return ret;
	}
	
	public static double generateRandomAngle()
	{
		Random rand = new Random();
		return rand.nextDouble() * 360;
	}
}
