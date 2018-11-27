package burlton.dartzee.test;

import java.awt.Point;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import burlton.dartzee.code.utils.GeometryUtil;
import burlton.core.code.obj.HandyArrayList;

/**
 * My first unit test. Woop!
 * 
 * 20/04/2018 - for posterity... 
 */
public class TestGeometryUtil
{
	@BeforeClass
	public static void initialiseTest()
	{
		new GeometryUtil(){}.toString();
	}
	
	@Test
	public void testTranslatePoint()
	{
		assertPointTranslation(5, 0, new Point(0, -5));
		assertPointTranslation(3, 90, new Point(3, 0));
		assertPointTranslation(-3, 90, new Point(-3, 0));
		assertPointTranslation(3, 270, new Point(-3, 0));
		assertPointTranslation(47, 180, new Point(0, 47));
		
		assertPointTranslation(5, 36.87, new Point(3, -4));
		assertPointTranslation(5, 53.13, new Point(4, -3));
		assertPointTranslation(5, 126.87, new Point(4, 3));
		assertPointTranslation(5, 216.87, new Point(-3, 4));
		assertPointTranslation(5, 306.87, new Point(-4, -3));
		
		//Edge case. It won't know what to do with this angle and should leave the point untouched
		assertPointTranslation(100, 500, new Point(0, 0));
	}
	private void assertPointTranslation(double radius, double degrees, Point expected)
	{
		Point pt = new Point(0, 0);
		Point result = GeometryUtil.translatePoint(pt, radius, degrees, false);
		
		String desc = "Translating (0, 0) by " + radius + " at an angle of " + degrees + " degrees";
		Assert.assertEquals(desc, expected, result);
	}

	@Test
	public void testGetDistance()
	{
		assertDistance(new Point(0, 0), new Point(3, 4), 5);
		assertDistance(new Point(0, 0), new Point(0, 7), 7); //Along y axis
		assertDistance(new Point(40, 0), new Point(0, 0), 40); //Along x axis
		assertDistance(new Point(-2, 5), new Point(3, -7), 13);
		assertDistance(new Point(2, 5), new Point(3, -7), 12.041);
	}
	private void assertDistance(Point ptOne, Point ptTwo, double expected)
	{
		String debug = "Distance from " + ptOne + " to " + ptTwo;
		
		Assert.assertEquals(debug, expected, GeometryUtil.getDistance(ptTwo, ptOne), 0.001);
		Assert.assertEquals(debug, expected, GeometryUtil.getDistance(ptOne, ptTwo), 0.001);
	}

	/**
	 * Measures the angle like below:
	 * 
	 * 			|     X
	 * 			|    X
	 * 			|^^ X
	 * 			|  X
	 * 			| X
	 * 			|X
	 * 	-------------------
	 */
	@Test
	public void testGetAngleForPoint()
	{
		Point centerPt = new Point(0, 0);
		
		//Go around in 45 degree increments
		assertAngle(new Point(1, 0), centerPt, 90);
		assertAngle(new Point(1, 1), centerPt, 45);
		assertAngle(new Point(0, 1), centerPt, 0);
		assertAngle(new Point(-1, 1), centerPt, 315);
		assertAngle(new Point(-1, 0), centerPt, 270);
		assertAngle(new Point(-1, -1), centerPt, 225);
		assertAngle(new Point(0, -1), centerPt, 180);
		assertAngle(new Point(1, -1), centerPt, 135);
		
		//Some inexact examples
		assertAngle(new Point(1, 2), centerPt, 26.565);
		assertAngle(new Point(1, 5), centerPt, 11.310);
	}
	private void assertAngle(Point pt, Point centerPt, double expected)
	{
		//Because Java points are stupid, and so "increasing" y takes you downwards. 
		//We've passed in the intuitive value. Transform it to java in here.
		pt.setLocation(pt.getX(), -pt.getY());
		
		double result = GeometryUtil.getAngleForPoint(pt, centerPt);
		
		Assert.assertEquals("Positive angle from " + centerPt + " to " + pt, expected, result, 0.01); 
	}

	@Test
	public void testGetAverage()
	{
		assertAverage(new Point(0, 0), new Point(0, 0));
		assertAverage(new Point(0, 0), new Point(1, 0), new Point(-1, 0));
		assertAverage(new Point(1, 1), new Point(0, 0), new Point(0, 2), new Point(2, 0), new Point(2, 2));
	}
	private void assertAverage(Point expected, Point... points)
	{
		String description = "Average point of " + points;
		ArrayList<Point> pointsArrayList = HandyArrayList.factoryAdd(points);
		Assert.assertEquals(description, GeometryUtil.getAverage(pointsArrayList), expected);
	}

	@Test
	public void testGenerateRandomAngle()
	{
		for (int i=0; i<1000; i++)
		{
			double angle = GeometryUtil.generateRandomAngle();
			Assert.assertTrue("Random angle is >= 0", angle >= 0);
			Assert.assertTrue("Random angle is <= 360", angle <= 360);
		}
	}

}
