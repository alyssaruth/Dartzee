package burlton.dartzee.code.utils;

import burlton.core.code.util.Debug;
import burlton.core.code.util.StringUtil;

import java.util.ArrayList;
import java.util.prefs.Preferences;

import static burlton.dartzee.code.utils.RegistryConstantsKt.NODE_PREFERENCES;

public class PreferenceUtil
{
	private static final String PREFERENCE_DELIM_CHAR = ";";
	
	private static final Preferences preferences = Preferences.userRoot().node(NODE_PREFERENCES);
	
	/**
	 * Helpers
	 */
	private static ArrayList<String> getPrefAndDefault(String prefStr)
	{
		ArrayList<String> prefAndDefault = StringUtil.getListFromDelims(prefStr, PREFERENCE_DELIM_CHAR);
		if (prefAndDefault.size() != 2)
		{
			Debug.stackTrace("Unexpected preference format: " + prefStr);
		}
		
		return prefAndDefault;
	}
	private static String getRawPref(String prefStr)
	{
		ArrayList<String> prefAndDefault = getPrefAndDefault(prefStr);
		return prefAndDefault.get(0);
	}
	
	/**
	 * Strings
	 */
	public static String getStringValue(String prefStr)
	{
		return getStringValue(prefStr, false);
	}
	public static String getStringValue(String prefStr, boolean returnDefault)
	{
		ArrayList<String> prefAndDefault = getPrefAndDefault(prefStr);
		
		String pref = prefAndDefault.get(0);
		String defaultVal = prefAndDefault.get(1);
		
		if (returnDefault)
		{
			return defaultVal;
		}
		
		//Return as normal
		return preferences.get(pref, defaultVal);
	}
	public static void saveString(String prefStr, String value)
	{
		String pref = getRawPref(prefStr);
		preferences.put(pref, value);
	}
	
	public static void deleteSetting(String prefStr)
	{
		String pref = getRawPref(prefStr);
		preferences.remove(pref);
	}

	/**
	 * Ints
	 */
	public static int getIntValue(String prefStr)
	{
		return getIntValue(prefStr, false);
	}
	public static int getIntValue(String prefStr, boolean returnDefault)
	{
		String ret = getStringValue(prefStr, returnDefault);
		return Integer.parseInt(ret);
	}
	public static void saveInt(String prefStr, int value)
	{
		String pref = getRawPref(prefStr);
		preferences.putInt(pref, value);
	}
	
	/**
	 * Doubles!
	 */
	public static double getDoubleValue(String prefStr)
	{
		return getDoubleValue(prefStr, false);
	}
	public static double getDoubleValue(String prefStr, boolean returnDefault)
	{
		String ret = getStringValue(prefStr, returnDefault);
		return Double.parseDouble(ret);
	}
	public static void saveDouble(String prefStr, double value)
	{
		String pref = getRawPref(prefStr);
		preferences.putDouble(pref, value);
	}
	
	/**
	 * Bools
	 */
	public static boolean getBooleanValue(String prefStr)
	{
		return getBooleanValue(prefStr, false);
	}
	public static boolean getBooleanValue(String prefStr, boolean returnDefault)
	{
		String ret = getStringValue(prefStr, returnDefault);
		return Boolean.parseBoolean(ret);
	}
	public static void saveBoolean(String prefStr, boolean value)
	{
		String pref = getRawPref(prefStr);
		preferences.putBoolean(pref, value);
	}
}
