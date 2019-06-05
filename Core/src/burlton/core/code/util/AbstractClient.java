package burlton.core.code.util;

import java.util.Locale;

/**
 * Interface used by Entropy Android & Desktop for anything to do with the online session
 */
public abstract class AbstractClient implements OnlineConstants
{
	public static final int SQL_MAX_DURATION = 5000; //5 seconds

	public static boolean devMode = false;
	public static boolean traceReadSql = true;
	public static boolean traceWriteSql = true;
	public static String logSecret = "";
	public static int sqlMaxDuration = SQL_MAX_DURATION;
	public static String operatingSystem = "";
	public static boolean justUpdated = false;
	public static String derbyDbName = "";
	
	//Instance
	private static AbstractClient client = null;
	
	/**
	 * Abstract methods
	 */
	public abstract void init();
	public abstract boolean isOnline();
	public abstract void handleResponse(String message, String encryptedResponse) throws Throwable;
	public abstract void checkForUpdates();
	
	/**
	 * Use these to show waiting dialogs/info to the user
	 */
	public abstract boolean isCommunicatingWithServer();
	public abstract void finishServerCommunication();
	public abstract void unableToConnect();
	public abstract void connectionLost();
	public abstract void goOffline();
	
	/**
	 * Helpers during startup
	 */
	public static void parseProgramArguments(String[] args)
	{
		for (int i=0; i<args.length; i++)
		{
			String arg = args[i];
			parseProgramArgument(arg);
		}
	}
	private static void parseProgramArgument(String arg)
	{
		if (arg.equals("justUpdated"))
		{
			justUpdated = true;
			Debug.append("Running in justUpdated mode");
		}
		else if (arg.equals("devMode"))
		{
			devMode = true;
			traceWriteSql = true;
			Debug.appendBanner("Running in dev mode");
		}
		else if (arg.equals("traceSql"))
		{
			traceWriteSql = true;
		}
		else if (arg.startsWith("logSecret="))
		{
			logSecret = arg.split("=")[1];
			Debug.append("Got logSecret - will be able to email logs");
		}
		else
		{
			Debug.append("Unexpected program argument: " + arg);
		}
	}
	public static void setOs()
	{
		operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
	}
	public static boolean isAppleOs()
	{
		return operatingSystem.contains("mac") || operatingSystem.contains("darwin");
	}
	
	public void checkForUpdatesIfRequired()
	{
		if (justUpdated)
		{
			Debug.append("Just updated - not checking for updates");
			return;
		}
		
		checkForUpdates();
	}
	
	public static AbstractClient getInstance()
	{
		return client;
	}
	public static void setInstance(AbstractClient client)
	{
		AbstractClient.client = client;
		client.init();
	}
}
