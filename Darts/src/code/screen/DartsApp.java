package code.screen;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import bean.AbstractDevScreen;
import code.db.sanity.DatabaseSanityCheck;
import code.screen.game.DartsGameScreen;
import code.utils.DartsDatabaseUtil;
import code.utils.DatabaseUtil;
import code.utils.DevUtilities;
import code.utils.ResourceCache;
import screen.DebugConsoleAdv;
import util.AbstractClient;
import util.Debug;
import util.DialogUtil;

public class DartsApp extends AbstractDevScreen
					  implements WindowListener
{
	private static final String CMD_PURGE_GAME = "purge ";
	private static final String CMD_LOAD_GAME = "load ";
	private static final String CMD_CLEAR_CONSOLE = "cls";
	private static final String CMD_TEST = "test";
	private static final String CMD_EMPTY_SCREEN_CACHE = "emptyscr";
	private static final String CMD_SANITY = "sanity";
	
	public DartsApp() 
	{
		super();
		setTitle("Darts");
		setSize(800, 600);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		getContentPane().add(commandBar, BorderLayout.SOUTH);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(this);
	}
	
	private EmbeddedScreen scrn = null;
	
	public void init()
	{
		setIcon();
		
		ResourceCache.initialiseResources();
		
		DartsDatabaseUtil.initialiseDatabase();
		
		addConsoleShortcut();
		switchScreen(ScreenCache.getScreen(MenuScreen.class));
		
		//Pop up the change log if we've just updated
		if (AbstractClient.justUpdated)
		{
			ChangeLog dialog = new ChangeLog();
			dialog.setVisible(true);
		}
	}
	
	private void setIcon()
	{
		String imageStr = "dartzee";
		
		//Load the four images corresponding to 16px, 32px, 64px and 128px
		ArrayList<Image> images = new ArrayList<>();
		for (int i=16; i<256; i=2*i)
		{
			Image ico = new ImageIcon(getClass().getResource("/icons/" + imageStr + i + ".png")).getImage();
			images.add(ico);
		}
		
		setIconImages(images);
	}
	
	private void addConsoleShortcut()
	{
		KeyStroke triggerStroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK);
		JPanel content = (JPanel)getContentPane();
		
		InputMap inputMap = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(triggerStroke, "showConsole");
		
		ActionMap actionMap = content.getActionMap();
		actionMap.put("showConsole", new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				DebugConsoleAdv loggingDialog = ScreenCache.getDebugConsole();
				loggingDialog.setVisible(true);
				loggingDialog.toFront();
			}
		});
	}
	
	public void switchScreen(EmbeddedScreen scrn)
	{
		switchScreen(scrn, true);
	}
	public void switchScreen(EmbeddedScreen scrn, boolean reInit)
	{
		try
		{
			if (reInit)
			{
				scrn.init();
			}
		}
		catch (Throwable t)
		{
			Debug.stackTraceNoError(t, "Failed to load screen " + scrn.getScreenName());
			DialogUtil.showError("Error loading screen - " + scrn.getScreenName());
			return;
		}
		
		if (this.scrn != null)
		{
			getContentPane().remove(this.scrn);
		}
		
		this.scrn = scrn;
		getContentPane().add(scrn, BorderLayout.CENTER);
		
		String screenName = scrn.getScreenName();
		setTitle("Darts - " + screenName);
		
		//Need pack() to ensure the dialog resizes correctly.
		//Need repaint() in case we don't resize.
		pack();
		repaint();
		
		scrn.postInit();
	}
	public EmbeddedScreen getCurrentScreen()
	{
		return scrn;
	}
	
	/**
	 * CheatListener
	 */
	@Override
	public boolean commandsEnabled()
	{
		return AbstractClient.devMode;
	}
	
	@Override
	public String processCommand(String cmd)
	{
		String textToShow = "";
		if (cmd.startsWith(CMD_PURGE_GAME))
		{
			String gameIdentifier = cmd.substring(CMD_PURGE_GAME.length());
			int gameId = Integer.parseInt(gameIdentifier);
			DevUtilities.purgeGame(gameId);
		}
		else if (cmd.startsWith(CMD_LOAD_GAME))
		{
			String gameIdentifier = cmd.substring(CMD_LOAD_GAME.length());
			int gameId = Integer.parseInt(gameIdentifier);
			DartsGameScreen.loadAndDisplayGame(gameId);
		}
		else if (cmd.equals(CMD_CLEAR_CONSOLE))
		{
			Debug.clearLogs();
		}
		else if (cmd.equals(CMD_TEST))
		{
			switchScreen(new TestScreen());
		}
		else if (cmd.equals("dim"))
		{
			Debug.append("Current screen size: " + getSize());
		}
		else if (cmd.equals(CMD_EMPTY_SCREEN_CACHE))
		{
			ScreenCache.emptyCache();
			Debug.append("Emptied screen cache.");
		}
		else if (cmd.equals(CMD_SANITY))
		{
			DatabaseSanityCheck.runSanityCheck();
		}
		else if (cmd.equals("convert"))
		{
			DatabaseUtil.executeUpdate("DELETE FROM Achievement");
			
			DartsDatabaseUtil.unlockV4Achievements();
		}
		
		return textToShow;
	}

	@Override
	public void windowActivated(WindowEvent arg0){}
	@Override
	public void windowClosed(WindowEvent arg0){}
	@Override
	public void windowDeactivated(WindowEvent arg0)
	{}
	@Override
	public void windowDeiconified(WindowEvent arg0)
	{}
	@Override
	public void windowIconified(WindowEvent arg0)
	{}
	@Override
	public void windowOpened(WindowEvent arg0)
	{}
	
	@Override
	public void windowClosing(WindowEvent arg0)
	{
		ScreenCache.exitApplication();
	}
}
