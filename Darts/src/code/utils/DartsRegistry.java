package code.utils;


public interface DartsRegistry 
{
	//Node names
	public static final String NODE_PREFERENCES = "DartsPrefs";
	
	//Variable names
	public static final String PREFERENCES_STRING_ODD_SINGLE_COLOUR = "oddsing;";
	public static final String PREFERENCES_STRING_ODD_DOUBLE_COLOUR = "odddoub;";
	public static final String PREFERENCES_STRING_ODD_TREBLE_COLOUR = "oddtreb;";
	public static final String PREFERENCES_STRING_EVEN_SINGLE_COLOUR = "evensing;";
	public static final String PREFERENCES_STRING_EVEN_DOUBLE_COLOUR = "evendoub;";
	public static final String PREFERENCES_STRING_EVEN_TREBLE_COLOUR = "eventreb;";
	
	public static final String PREFERENCES_BOOLEAN_DISPLAY_DART_TOTAL_SCORE = "drttotsc;true";
	public static final String PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE = "aiauto;true";
	public static final String PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES = "chkupd;true";
	public static final String PREFERENCES_BOOLEAN_SHOW_ANIMATIONS = "anim;true";
	
	public static final String PREFERENCES_INT_AI_SPEED = "aispd;1000";
	public static final String PREFERENCES_INT_LEADERBOARD_SIZE = "ldbrdsz;50";
	
	public static final String PREFERENCES_DOUBLE_HUE_FACTOR = "huefactor;0.8";
	public static final String PREFERENCES_DOUBLE_FG_BRIGHTNESS = "fgbri;0.5";
	public static final String PREFERENCES_DOUBLE_BG_BRIGHTNESS = "bgbri;1";
}
