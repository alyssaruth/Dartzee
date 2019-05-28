package burlton.dartzee.code.screen;

import burlton.core.code.obj.SuperHashMap;
import burlton.core.code.util.Debug;
import burlton.dartzee.code.screen.ai.AIConfigurationDialog;
import burlton.dartzee.code.screen.game.DartsGameScreen;
import burlton.dartzee.code.screen.preference.PreferencesDialog;
import burlton.dartzee.code.screen.reporting.ConfigureReportColumnsDialog;
import burlton.desktopcore.code.bean.CheatBar;
import burlton.desktopcore.code.screen.BugReportDialog;
import burlton.desktopcore.code.screen.DebugConsole;
import burlton.desktopcore.code.util.DialogUtil;

import javax.swing.*;
import java.util.ArrayList;

public final class ScreenCache 
{
	private static SuperHashMap<String, DartsGameScreen> hmGameIdToGameScreen = new SuperHashMap<>();
	
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
	private static DebugConsole debugConsole = null;
	
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
			mainScreen = new DartsApp(new CheatBar());
		}
		
		return mainScreen;
	}

	public static <K extends EmbeddedScreen> void switchScreen(Class<K> screenClass)
	{
		K screen = getScreen(screenClass);
		switchScreen(screen);
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
	
	public static DebugConsole getDebugConsole()
	{
		if (debugConsole == null)
		{
			debugConsole = new DebugConsole();
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
	public static DartsGameScreen getDartsGameScreen(String gameId)
	{
		return hmGameIdToGameScreen.get(gameId);
	}
	public static void addDartsGameScreen(String gameId, DartsGameScreen scrn)
	{
		if (gameId.isEmpty())
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
