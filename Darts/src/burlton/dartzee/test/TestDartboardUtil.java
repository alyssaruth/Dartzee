package burlton.dartzee.test;

import java.awt.Color;
import java.awt.Point;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import burlton.dartzee.code.object.ColourWrapper;
import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.object.DartboardSegment;
import burlton.dartzee.code.utils.DartboardUtil;
import burlton.dartzee.code.utils.DartsColour;
import burlton.dartzee.code.utils.DartsRegistry;
import burlton.dartzee.code.utils.PreferenceUtil;

public class TestDartboardUtil implements DartsRegistry
{
	private static String evenSingleStr = null;
	private static String evenDoubleStr = null;
	private static String evenTrebleStr = null;
	private static String oddSingleStr = null;
	private static String oddDoubleStr = null;
	private static String oddTrebleStr = null;
	
	@BeforeClass
	public static void initialiseTest()
	{
		evenSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_SINGLE_COLOUR);
		evenDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR);
		evenTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_TREBLE_COLOUR);
		oddSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_SINGLE_COLOUR);
		oddDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_DOUBLE_COLOUR);
		oddTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_TREBLE_COLOUR);
		
		new DartboardUtil().toString();
	}
	
	@AfterClass
	public static void tearDown()
	{
		PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_SINGLE_COLOUR, evenSingleStr);
		PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR, evenDoubleStr);
		PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_TREBLE_COLOUR, evenTrebleStr);
		PreferenceUtil.saveString(PREFERENCES_STRING_ODD_SINGLE_COLOUR, oddSingleStr);
		PreferenceUtil.saveString(PREFERENCES_STRING_ODD_DOUBLE_COLOUR, oddDoubleStr);
		PreferenceUtil.saveString(PREFERENCES_STRING_ODD_TREBLE_COLOUR, oddTrebleStr);
	}

	@Test
	public void testFactorySegmentKeyForPoint()
	{
		PreferenceUtil.deleteSetting(PREFERENCES_STRING_EVEN_SINGLE_COLOUR);
		PreferenceUtil.deleteSetting(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR);
		PreferenceUtil.deleteSetting(PREFERENCES_STRING_EVEN_TREBLE_COLOUR);
		PreferenceUtil.deleteSetting(PREFERENCES_STRING_ODD_SINGLE_COLOUR);
		PreferenceUtil.deleteSetting(PREFERENCES_STRING_ODD_DOUBLE_COLOUR);
		PreferenceUtil.deleteSetting(PREFERENCES_STRING_ODD_TREBLE_COLOUR);
		
		DartboardUtil.resetCachedValues();
		
		//Bullseyes
		assertSegment(new Point(0, 0), DartboardSegment.TYPE_DOUBLE, 25, 2, DartsColour.DARTBOARD_RED);
		assertSegment(new Point(37, 0), DartboardSegment.TYPE_DOUBLE, 25, 2, DartsColour.DARTBOARD_RED);
		assertSegment(new Point(38, 0), DartboardSegment.TYPE_OUTER_SINGLE, 25, 1, DartsColour.DARTBOARD_GREEN);
		assertSegment(new Point(48, 55), DartboardSegment.TYPE_OUTER_SINGLE, 25, 1, DartsColour.DARTBOARD_GREEN);
		assertSegment(new Point(0, -93), DartboardSegment.TYPE_OUTER_SINGLE, 25, 1, DartsColour.DARTBOARD_GREEN);
		
		//Boundary conditions for varying radius
		assertSegment(new Point(0, 94), DartboardSegment.TYPE_INNER_SINGLE, 3, 1, DartsColour.DARTBOARD_BLACK);
		assertSegment(new Point(0, 581), DartboardSegment.TYPE_INNER_SINGLE, 3, 1, DartsColour.DARTBOARD_BLACK);
		assertSegment(new Point(0, -582), DartboardSegment.TYPE_TREBLE, 20, 3, DartsColour.DARTBOARD_RED);
		assertSegment(new Point(0, -628), DartboardSegment.TYPE_TREBLE, 20, 3, DartsColour.DARTBOARD_RED);
		assertSegment(new Point(629, 0), DartboardSegment.TYPE_OUTER_SINGLE, 6, 1, DartsColour.DARTBOARD_WHITE);
		assertSegment(new Point(-952, 0), DartboardSegment.TYPE_OUTER_SINGLE, 11, 1, DartsColour.DARTBOARD_WHITE);
		assertSegment(new Point(953, 0), DartboardSegment.TYPE_DOUBLE, 6, 2, DartsColour.DARTBOARD_GREEN);
		assertSegment(new Point(0, -999), DartboardSegment.TYPE_DOUBLE, 20, 2, DartsColour.DARTBOARD_RED);
		assertSegment(new Point(0, -1000), DartboardSegment.TYPE_MISS, 20, 0, Color.black);
		assertSegment(new Point(0, -1299), DartboardSegment.TYPE_MISS, 20, 0, Color.black);
		assertSegment(new Point(0, -1300), DartboardSegment.TYPE_MISSED_BOARD, 20, 0, null);
		
		//Test 45 degrees etc
		assertSegment(new Point(100, -100), DartboardSegment.TYPE_INNER_SINGLE, 4, 1, DartsColour.DARTBOARD_WHITE);
		assertSegment(new Point(-100, -100), DartboardSegment.TYPE_INNER_SINGLE, 9, 1, DartsColour.DARTBOARD_WHITE);
		assertSegment(new Point(-100, 100), DartboardSegment.TYPE_INNER_SINGLE, 7, 1, DartsColour.DARTBOARD_BLACK);
		assertSegment(new Point(100, 100), DartboardSegment.TYPE_INNER_SINGLE, 15, 1, DartsColour.DARTBOARD_WHITE);
	}
	private void assertSegment(Point pt, int segmentType, int score, int multiplier, Color expectedColor)
	{
		String key = DartboardUtil.factorySegmentKeyForPoint(pt, new Point(0, 0), 2000);
		String expectedKey = score + "_" + segmentType;
		Assert.assertEquals("SegmentKey for point " + pt + " for dartboard with radius 1000", expectedKey, key);
		
		DartboardSegment segment = new DartboardSegment(key);
		
		String segmentStr = "" + segment;
		Assert.assertEquals("DartboardSegment.toString() for point " + pt, score + " (" + segmentType + ")", segmentStr);
		
		Dart drt = DartboardUtil.getDartForSegment(pt, segment);
		
		Assert.assertEquals("Dart score for point " + pt, score, drt.getScore());
		Assert.assertEquals("Dart multiplier for point " + pt, multiplier, drt.getMultiplier());
		Assert.assertEquals("SegmentType for point " + pt, segmentType, drt.getSegmentType());
		
		assertColourForPointAndSegment(pt, segment, null, expectedColor, false);
	}

	@Test
	public void testResetCachedValues()
	{
		DartboardUtil.resetCachedValues();
		Color pink = Color.pink;
		PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_SINGLE_COLOUR, DartsColour.toPrefStr(pink));
		assertSegment(new Point(0, -629), DartboardSegment.TYPE_OUTER_SINGLE, 20, 1, pink);
	}

	
	@Test
	public void testHighlights()
	{
		ColourWrapper wrapper = new ColourWrapper(Color.BLACK, Color.RED, Color.RED, Color.WHITE, Color.GREEN, Color.GREEN, Color.RED, Color.GREEN);
		DartboardSegment segment = new DartboardSegment("20_4");
		
		assertColourForPointAndSegment(new Point(0, 0), segment, wrapper, Color.BLACK, false);
		assertColourForPointAndSegment(new Point(0, 0), segment, wrapper, Color.BLACK.darker().darker(), true);
	}
	
	@Test
	public void testWireframe()
	{
		ColourWrapper wrapper = new ColourWrapper(null);
		wrapper.setEdgeColour(Color.YELLOW);
		
		DartboardSegment fakeSegment = new DartboardSegment("20_1");
		for (int x=0; x<=200; x++)
		{
			for (int y=0; y<=200; y++)
			{
				fakeSegment.addPoint(new Point(x, y));
			}
		}
		
		int pointsTotal = fakeSegment.getPoints().size();
		Assert.assertEquals("Total points in [0, 200] x [0, 200]", 40401, pointsTotal);
		
		//Four corners and four edge mid-points
		assertColourForPointAndSegment(new Point(0, 0), fakeSegment, wrapper, Color.YELLOW, false);
		assertColourForPointAndSegment(new Point(0, 100), fakeSegment, wrapper, Color.YELLOW, false);
		assertColourForPointAndSegment(new Point(100, 0), fakeSegment, wrapper, Color.YELLOW, false);
		assertColourForPointAndSegment(new Point(0, 200), fakeSegment, wrapper, Color.YELLOW, false);
		assertColourForPointAndSegment(new Point(200, 0), fakeSegment, wrapper, Color.YELLOW, false);
		assertColourForPointAndSegment(new Point(200, 100), fakeSegment, wrapper, Color.YELLOW, false);
		assertColourForPointAndSegment(new Point(100, 200), fakeSegment, wrapper, Color.YELLOW, false);
		assertColourForPointAndSegment(new Point(200, 200), fakeSegment, wrapper, Color.YELLOW, false);
		
		//Non-edge boundary cases
		assertColourForPointAndSegment(new Point(1, 1), fakeSegment, wrapper, null, false);
		assertColourForPointAndSegment(new Point(199, 1), fakeSegment, wrapper, null, false);
		assertColourForPointAndSegment(new Point(1, 199), fakeSegment, wrapper, null, false);
		assertColourForPointAndSegment(new Point(199, 199), fakeSegment, wrapper, null, false);
		
		//Another non-edge. Let's say we'll highlight this one, to check theres no NPE
		assertColourForPointAndSegment(new Point(100, 100), fakeSegment, wrapper, null, true);
		
		//Now assign this to be a "miss" segment. We should no longer get the wireframe, even for an edge
		fakeSegment.setType(DartboardSegment.TYPE_MISS);
		assertColourForPointAndSegment(new Point(0, 0), fakeSegment, wrapper, null, false);
		assertColourForPointAndSegment(new Point(1, 1), fakeSegment, wrapper, null, false);
	}
	private void assertColourForPointAndSegment(Point pt, DartboardSegment segment, ColourWrapper wrapper, Color expected, boolean highlight)
	{
		Color color = DartboardUtil.getColourForPointAndSegment(pt, segment, highlight, wrapper);
		Assert.assertEquals("Color for point " + pt + " and segment " + segment, expected, color);
	}
}
