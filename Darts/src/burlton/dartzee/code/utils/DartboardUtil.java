package burlton.dartzee.code.utils;

import burlton.dartzee.code.object.ColourWrapper;
import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.object.DartboardSegment;
import burlton.dartzee.code.object.DartboardSegmentKt;

import java.awt.*;
import java.util.HashMap;

/**
 * Utility class for the Dartboard object.
 */
public class DartboardUtil implements DartsRegistry
{
	private static HashMap<Integer, Boolean> hmScoreToOrdinal = null;
	private static ColourWrapper colourWrapperFromPrefs = null;
	
	private static int[] numberOrder = {20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5, 20};
	
	private static final double RATIO_INNER_BULL = 0.038;
	private static final double RATIO_OUTER_BULL = 0.094;
	private static final double LOWER_BOUND_TRIPLE_RATIO = 0.582;
	private static final double UPPER_BOUND_TRIPLE_RATIO = 0.629;
	private static final double LOWER_BOUND_DOUBLE_RATIO = 0.953;
	
	private static final double UPPER_BOUND_DOUBLE_RATIO = 1;
	public static final double UPPER_BOUND_OUTSIDE_BOARD_RATIO = 1.3;
	
	public static Dart getDartForSegment(Point pt, DartboardSegmentKt segment)
	{
		int score = segment.getScore();
		int multiplier = segment.getMultiplier();
		return new Dart(score, multiplier, pt, segment.getType());
	}
	
	public static String factorySegmentKeyForPoint(Point dartPt, Point centerPt, double diameter)
	{
		double radius = GeometryUtil.getDistance(dartPt, centerPt);
		double ratio = 2*radius/diameter;
		
		if (ratio < RATIO_INNER_BULL)
		{
			return 25 + "_" + DartboardSegment.TYPE_DOUBLE;
		}
		else if (ratio < RATIO_OUTER_BULL)
		{
			return 25 + "_" + DartboardSegment.TYPE_OUTER_SINGLE;
		}
		
		//We've not hit the bullseye, so do other calculations to work out score/multiplier
		double angle = GeometryUtil.getAngleForPoint(dartPt, centerPt);
		int score = getScoreForAngle(angle);
		int type = calculateTypeForRatioNonBullseye(ratio);
		
		return score + "_" + type;
	}
	
	/**
	 * 1) Calculate the radius from the center to our point
	 * 2) Using the diameter, work out whether this makes us a miss, single, double or treble
	 */
	private static int calculateTypeForRatioNonBullseye(double ratioToDiameter)
	{
		if (ratioToDiameter < LOWER_BOUND_TRIPLE_RATIO)
		{
			return DartboardSegment.TYPE_INNER_SINGLE;
		}
		else if (ratioToDiameter < UPPER_BOUND_TRIPLE_RATIO)
		{
			return DartboardSegment.TYPE_TREBLE;
		}
		else if (ratioToDiameter < LOWER_BOUND_DOUBLE_RATIO)
		{
			return DartboardSegment.TYPE_OUTER_SINGLE;
		}
		else if (ratioToDiameter < UPPER_BOUND_DOUBLE_RATIO)
		{
			return DartboardSegment.TYPE_DOUBLE;
		}
		else if (ratioToDiameter < UPPER_BOUND_OUTSIDE_BOARD_RATIO)
		{
			return DartboardSegment.TYPE_MISS;
		}
		
		return DartboardSegment.TYPE_MISSED_BOARD;
	}
	
	private static int getScoreForAngle(double angle)
	{
		int checkValue = 9;
		int index = 0;
		while (angle > checkValue)
		{
			index++;
			checkValue += 18;
		}
		
		return numberOrder[index];
	}
	
	public static Color getColourForPointAndSegment(Point pt, DartboardSegmentKt segment, boolean highlighted,
	  ColourWrapper colourWrapper)
	{
		//For normal gameplay, we'll pass in null and get the colours from the user pref.
		if (colourWrapper == null)
		{
			colourWrapper = getColourWrapperFromPrefs();
		}
		
		Color edgeColour = colourWrapper.getEdgeColour();
		if (edgeColour != null
    	  && !segment.isMiss()
    	  && segment.isEdgePoint(pt))
    	{
    		return edgeColour;
    	}
		
		Color colour = getColourFromHashMap(segment, colourWrapper);
		if (highlighted)
		{
			return DartsColour.getDarkenedColour(colour);
		}
		
		return colour;
	}
	private static Color getColourFromHashMap(DartboardSegmentKt segment, ColourWrapper colourWrapper)
	{
		int type = segment.getType();
		if (type == DartboardSegment.TYPE_MISS)
		{
			return colourWrapper.getOuterDartboardColour();
		}
		
		if (type == DartboardSegment.TYPE_MISSED_BOARD)
		{
			return colourWrapper.getMissedBoardColour();
		}
		
		if (hmScoreToOrdinal == null)
		{
			initialiseOrdinalHashMap();
		}
		
		int score = segment.getScore();
		int multiplier = segment.getMultiplier();
		
		if (score == 25)
		{
			return colourWrapper.getBullColour(multiplier);
		}
		
		boolean even = hmScoreToOrdinal.get(Integer.valueOf(score));
		return colourWrapper.getColour(multiplier, even);
	}
	private static ColourWrapper getColourWrapperFromPrefs()
	{
		if (colourWrapperFromPrefs != null)
		{
			return colourWrapperFromPrefs;
		}
		
		String evenSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_SINGLE_COLOUR);
		String evenDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR);
		String evenTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_TREBLE_COLOUR);
		String oddSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_SINGLE_COLOUR);
		String oddDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_DOUBLE_COLOUR);
		String oddTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_TREBLE_COLOUR);
		
		Color evenSingle = DartsColour.getColorFromPrefStr(evenSingleStr, DartsColour.DARTBOARD_BLACK);
		Color evenDouble = DartsColour.getColorFromPrefStr(evenDoubleStr, DartsColour.DARTBOARD_RED);
		Color evenTreble = DartsColour.getColorFromPrefStr(evenTrebleStr, DartsColour.DARTBOARD_RED);
		
		Color oddSingle = DartsColour.getColorFromPrefStr(oddSingleStr, DartsColour.DARTBOARD_WHITE);
		Color oddDouble = DartsColour.getColorFromPrefStr(oddDoubleStr, DartsColour.DARTBOARD_GREEN);
		Color oddTreble = DartsColour.getColorFromPrefStr(oddTrebleStr, DartsColour.DARTBOARD_GREEN);
		
		colourWrapperFromPrefs = new ColourWrapper(evenSingle, evenDouble, evenTreble,
				oddSingle, oddDouble, oddTreble, evenDouble, oddDouble);
		
		return colourWrapperFromPrefs;
	}
	
	private static void initialiseOrdinalHashMap()
	{
		HashMap<Integer, Boolean> ret = new HashMap<>();
		
		for (int i=0; i<numberOrder.length - 1; i++)
		{
			boolean even = ((i&1) == 0);
			ret.put(Integer.valueOf(numberOrder[i]), even);
		}
		
		hmScoreToOrdinal = ret;
	}
	
	public static void resetCachedValues()
	{
		colourWrapperFromPrefs = null;
	}
}
