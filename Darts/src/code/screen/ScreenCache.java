package code.screen;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import code.screen.ai.AIConfigurationDialog;
import code.screen.game.DartsGameScreen;
import code.screen.preference.PreferencesDialog;
import code.screen.reporting.ConfigureReportColumnsDialog;
import object.SuperHashMap;
import screen.BugReportDialog;
import screen.DebugConsoleAdv;
import util.Debug;
import util.DialogUtil;

public final class ScreenCache 
{
	private static SuperHashMap<Long, DartsGameScreen> hmGameIdToGameScreen = new SuperHashMap<>();
	
	//Embedded screens
	private static SuperHashMap<Class<? extends EmbeddedScreen>, EmbeddedScreen> hmClassToScreen = new SuperHashMap<>();
	private static DartsApp mainScreen = null;
	
	//Dialogs
	private static HumanCreationDialog humanCreationDialog = null;
	private static AIConfigurationDialog aiConfigurationDialog = null;
	private static AboutDialogDarts aboutDialog = null;
	private static BugReportDialog bugReportDialog = null;
	private static PreferencesDialog preferencesDialog = null;
	private static PlayerImageDialog playerImageDialog = null;
	private static ConfigureReportColumnsDialog configureReportColumnsDialog = null;
	
	//Other
	private static DebugConsoleAdv debugConsole = null;
	
	public static <K extends EmbeddedScreen> K getScreen(Class<K> screenClass)
	{
		K scrn = (K)hmClassToScreen.get(screenClass);
				
		try
		{
			if (scrn == null)
			{
				scrn = screenClass.newInstance();
				hmClassToScreen.put(screenClass, scrn);
			}
		}
		catch (IllegalAccessException | InstantiationException iae)
		{
			Debug.stackTrace(iae);
			DialogUtil.showError("Error loading screen.");
		}
		
		return scrn;
	}
	
	public static DartsApp getMainScreen()
	{
		if (mainScreen == null)
		{
			mainScreen = new DartsApp();
		}
		
		return mainScreen;
	}
	
	public static void switchScreen(EmbeddedScreen scrn)
	{
		switchScreen(scrn, true);
	}
	public static void switchScreen(EmbeddedScreen scrn, boolean reInit)
	{
		getMainScreen().switchScreen(scrn, reInit);
	}
	
	public static AIConfigurationDialog getAIConfigurationDialog()
	{
		if (aiConfigurationDialog == null)
		{
			aiConfigurationDialog = new AIConfigurationDialog();
		}
		
		aiConfigurationDialog.setLocationRelativeTo(getMainScreen());
		return aiConfigurationDialog;
	}
	
	public static HumanCreationDialog getHumanCreationDialog()
	{
		if (humanCreationDialog == null)
		{
			humanCreationDialog = new HumanCreationDialog();
		}
		
		humanCreationDialog.setLocationRelativeTo(getMainScreen());
		return humanCreationDialog;
	}
	
	public static PlayerManagementScreen getPlayerManagementScreen()
	{
		return getScreen(PlayerManagementScreen.class);
	}
	
	public static DebugConsoleAdv getDebugConsole()
	{
		if (debugConsole == null)
		{
			debugConsole = new DebugConsoleAdv();
		}
		
		return debugConsole;
	}
	
	public static AboutDialogDarts getAboutDialog() 
	{
		if (aboutDialog == null)
		{
			aboutDialog = new AboutDialogDarts();
		}
		return aboutDialog;
	}
	
	public static BugReportDialog getBugReportDialog() 
	{
		if (bugReportDialog == null)
		{
			bugReportDialog = new BugReportDialog();
		}
		return bugReportDialog;
	}
	
	public static PreferencesDialog getPreferencesDialog()
	{
		if (preferencesDialog == null)
		{
			preferencesDialog = new PreferencesDialog();
		}
		
		preferencesDialog.init();
		return preferencesDialog;
	}
	
	public static ArrayList<DartsGameScreen> getDartsGameScreens()
	{
		return hmGameIdToGameScreen.getValuesAsVector(true);
	}
	public static DartsGameScreen getDartsGameScreen(long gameId)
	{
		return hmGameIdToGameScreen.get(gameId);
	}
	public static void addDartsGameScreen(long gameId, DartsGameScreen scrn)
	{
		if (gameId == -1)
		{
			Debug.stackTrace("Trying to cache GameScreen with no gameId.");
			return;
		}
		
		hmGameIdToGameScreen.put(gameId, scrn);
	}
	public static void removeDartsGameScreen(DartsGameScreen scrn)
	{
		hmGameIdToGameScreen.removeAllWithValue(scrn);
	}
	
	public static PlayerImageDialog getPlayerImageDialog()
	{
		if (playerImageDialog == null)
		{
			playerImageDialog = new PlayerImageDialog();
		}
		
		return playerImageDialog;
	}
	
	public static ConfigureReportColumnsDialog getConfigureReportColumnsDialog()
	{
		if (configureReportColumnsDialog == null)
		{
			configureReportColumnsDialog = new ConfigureReportColumnsDialog();
		}
		
		return configureReportColumnsDialog;
	}
	
	public static void exitApplication()
	{
		ArrayList<DartsGameScreen> openGames = getDartsGameScreens();
		int size = openGames.size();
		if (size > 0)
		{
			int ans = DialogUtil.showQuestion("Are you sure you want to exit? There are " + size + " game window(s) still open.", false);
			if (ans == JOptionPane.NO_OPTION)
			{
				return;
			}
		}
		
		System.exit(0);
	}
	
	public static void emptyCache()
	{
		hmClassToScreen.clear();
		
		humanCreationDialog = null;
		aiConfigurationDialog = null;
		aboutDialog = null;
		bugReportDialog = null;
		preferencesDialog = null;
		playerImageDialog = null;
		configureReportColumnsDialog = null;
	}
}
