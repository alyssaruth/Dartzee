package burlton.core.code.util;

public interface OnlineConstants 
{
	//Used to make sure client/server are in sync
	public static final String SERVER_VERSION = "19";
	
	//Used in the AboutDialog and for sending logs
	public static final String ENTROPY_VERSION_NUMBER = "v7.0.0";
	public static final String DARTS_VERSION_NUMBER = "v3.0.0";
	
	//Filenames - for automatic updates
	public static final String FILE_NAME_DARTS = "Dartzee.jar";
	public static final String FILE_NAME_ENTROPY_JAR = "EntropyLive.jar";
	
	//Port numbers
	//Live
	public static final int SERVER_PORT_NUMBER_LOWER_BOUND = 1142;
	public static final int SERVER_PORT_NUMBER_UPPER_BOUND = 1152;
	public static final int SERVER_PORT_NUMBER_DOWNLOAD = 1153; //Also in EntropyUpdater
	public static final int SERVER_PORT_NUMBER_DOWNLOAD_DARTS = 1154;
	
	public static final String LOBBY_ID = "Lobby";
}
