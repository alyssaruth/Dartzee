package code.utils;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import util.Debug;
import util.StringUtil;

public final class DartsColour implements DartsRegistry
{
	public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
	
	public static final Color DARTBOARD_RED = Color.red;
	public static final Color DARTBOARD_GREEN = Color.green;
	public static final Color DARTBOARD_BLACK = Color.getHSBColor(0, 0, (float)0.1);
	public static final Color DARTBOARD_WHITE = Color.white;
	
	public static final Color DARTBOARD_LIGHTEST_GREY = Color.getHSBColor(0, 0, (float)0.9);
	public static final Color DARTBOARD_LIGHTER_GREY = Color.getHSBColor(0, 0, (float)0.75);
	public static final Color DARTBOARD_LIGHT_GREY = Color.getHSBColor(0, 0, (float)0.6);
	
	public static final Color COLOUR_GOLD_TEXT = Color.getHSBColor((float)5/36, 1, (float)0.8);
	public static final Color COLOUR_SILVER_TEXT = Color.GRAY.darker().darker();
	
	public static final Color COLOUR_BRONZE = Color.getHSBColor((float)45/360, (float)0.91, (float)0.49);
	public static final Color COLOUR_BRONZE_TEXT = COLOUR_BRONZE.darker().darker();
	
	public static Color getDarkenedColour(Color colour)
	{
		if (colour == null)
		{
			return colour;
		}
		
		Color brighterColour = colour.darker().darker();
		return brighterColour;
	}
	public static Color getBrightenedColour(Color colour)
	{
		float[] hsbValues = new float[3];
		hsbValues = Color.RGBtoHSB(colour.getRed(), colour.getGreen(), colour.getBlue(), hsbValues);
		
		float decreasedSat = Math.max(0, (float)(hsbValues[1] - 0.5));
		return Color.getHSBColor(hsbValues[0], decreasedSat, hsbValues[2]);
	}
	
	public static String toPrefStr(Color colour)
	{
		int r = colour.getRed();
		int g = colour.getGreen();
		int b = colour.getBlue();
		int a = colour.getAlpha();
		
		return r + ";" + g + ";" + b + ";" + a;
	}
	
	public static Color getColorFromPrefStr(String prefsStr, Color defaultColor)
	{
		if (prefsStr.isEmpty())
		{
			return defaultColor;
		}
		
		return fromPrefStr(prefsStr);
	}
	
	private static Color fromPrefStr(String prefStr)
	{
		Color ret = null;
		
		try
		{
			ArrayList<String> colours = StringUtil.getListFromDelims(prefStr, ";");
			
			int r = Integer.parseInt(colours.get(0));
			int g = Integer.parseInt(colours.get(1));
			int b = Integer.parseInt(colours.get(2));
			int a = Integer.parseInt(colours.get(3));
			
			ret = new Color(r, g, b, a);
		}
		catch (Throwable t)
		{
			Debug.stackTrace("Failed to reconstruct colour from string: " + prefStr);
		}
		
		return ret;
	}
	
	public static void setFgAndBgColoursForPosition(Component c, int finishPos)
	{
		setFgAndBgColoursForPosition(c, finishPos, null);
	}
	public static void setFgAndBgColoursForPosition(Component c, int finishPos, Color defaultBg)
	{
		if (finishPos == 1)
		{
			c.setBackground(Color.YELLOW);
			c.setForeground(DartsColour.COLOUR_GOLD_TEXT);
		}
		else if (finishPos == 2)
		{
			c.setBackground(Color.GRAY);
			c.setForeground(DartsColour.COLOUR_SILVER_TEXT);
		}
		else if (finishPos == 3)
		{
			c.setBackground(DartsColour.COLOUR_BRONZE);
			c.setForeground(DartsColour.COLOUR_BRONZE_TEXT);
		}
		else if (finishPos == 4)
		{
			c.setBackground(Color.BLACK);
			c.setForeground(DartsColour.COLOUR_BRONZE);
		}
		else
		{
			c.setBackground(defaultBg);
			c.setForeground(null);
		}
	}
	
	
	public static Color getScorerForegroundColour(double totalScore)
	{
		double hueFactor = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_HUE_FACTOR);
		double fgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_FG_BRIGHTNESS);
		return getScorerColour(totalScore, hueFactor, fgBrightness);
	}
	public static Color getScorerBackgroundColour(double totalScore)
	{
		double hueFactor = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_HUE_FACTOR);
		double bgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_BG_BRIGHTNESS);
		return getScorerColour(totalScore, hueFactor, bgBrightness);
	}
	public static Color getScorerColour(double totalScore, double multiplier, double brightness)
	{
		float hue = (float)(totalScore * multiplier) / 180;
		return Color.getHSBColor(hue, 1, (float)brightness);
	}
}
