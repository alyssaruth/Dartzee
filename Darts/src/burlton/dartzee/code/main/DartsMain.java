package burlton.dartzee.code.main;

import javax.swing.UIManager;

import burlton.dartzee.code.screen.DartsApp;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.dartzee.code.utils.DartsDebugExtension;
import burlton.dartzee.code.utils.DartsRegistry;
import burlton.dartzee.code.utils.DesktopDartsClient;
import burlton.dartzee.code.utils.PreferenceUtil;
import burlton.core.code.util.AbstractClient;
import burlton.core.code.util.Debug;
import burlton.core.code.util.DebugUncaughtExceptionHandler;
import burlton.desktopcore.code.util.DialogUtil;
import burlton.core.code.util.OnlineConstants;

public class DartsMain implements DartsRegistry
{
	public static void main(String[] args) 
	{
		Debug.initialise(ScreenCache.getDebugConsole());
		AbstractClient.setInstance(new DesktopDartsClient());
		
		setLookAndFeel();
		
		Debug.setDebugExtension(new DartsDebugExtension());
		Debug.setProductDesc("Darts " + OnlineConstants.DARTS_VERSION_NUMBER);
		Debug.setLogToSystemOut(true);
		
		DartsApp mainScreen = ScreenCache.getMainScreen();
		Thread.setDefaultUncaughtExceptionHandler(new DebugUncaughtExceptionHandler());
		
		AbstractClient.parseProgramArguments(args);
		
		Debug.setSendingEmails(!AbstractClient.devMode);
		
		checkForUpdatesIfRequired();
		
		mainScreen.setVisible(true);
		mainScreen.init();
	}
	
	private static void setLookAndFeel()
	{
		AbstractClient.setOs();
		Debug.append("Initialising Look & Feel - Operating System: " + AbstractClient.operatingSystem);
		if (AbstractClient.isAppleOs())
		{
			setLookAndFeel("javax.swing.plaf.metal");
		}
		else
		{
			setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		}
	}
	
	private static void setLookAndFeel(String laf)
	{
		try
		{
			UIManager.setLookAndFeel(laf);
		} 
		catch (Exception e) 
		{
		    Debug.append("Failed to load LookAndFeel " + laf + ". Caught " + e);
		    DialogUtil.showError("Failed to load Look & Feel 'Nimbus'.");
		}
	}
	
	private static void checkForUpdatesIfRequired()
	{
		if (AbstractClient.devMode)
		{
			Debug.append("Not checking for updates as I'm in dev mode");
			return;
		}
		
		if (!PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES))
		{
			Debug.append("Not checking for updates as preference is disabled");
			return;
		}
		
		AbstractClient.getInstance().checkForUpdatesIfRequired();
	}
}
