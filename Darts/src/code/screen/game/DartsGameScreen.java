package code.screen.game;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import code.db.DartsMatchEntity;
import code.db.GameEntity;
import code.db.ParticipantEntity;
import code.db.PlayerEntity;
import code.screen.ScreenCache;
import object.HandyArrayList;
import object.SuperHashMap;
import util.DateUtil;
import util.Debug;
import util.DialogUtil;

/**
 * DartsGameScreen
 * Simple screen which wraps up either a single game panel, or multiple tabs for a match.
 */
public final class DartsGameScreen extends JFrame
									  implements WindowListener, ChangeListener
{
	private DartsMatchEntity match = null;
	
	public DartsGameScreen()
	{
		setSize(880, 675);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		tabbedPane.addTab("Match", matchPanel);
		tabbedPane.addChangeListener(this);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(this);
	}
	
	private final MatchSummaryPanelMk2 matchPanel = new MatchSummaryPanelMk2();
	private final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
	private final SuperHashMap<Long, DartsGamePanel<? extends DartsScorer>> hmGameIdToTab = new SuperHashMap<>();
	
	/**
	 * Init
	 */
	public void initSingleGame(GameEntity game, int totalPlayers)
	{
		//Re-size the screen based on how many players there are
		setScreenSize(totalPlayers);
		
		//Cache this screen in ScreenCache
		long gameId = game.getRowId();
		ScreenCache.addDartsGameScreen(gameId, this);
		
		//Initialise some basic properties of the tab, such as visibility of components etc
		DartsGamePanel<? extends DartsScorer> tab = DartsGamePanel.factory(this, game.getGameType());
		tab.initBasic(game, totalPlayers);
		
		String title = tab.getGameTitle();
		setTitle(title);
		
		//Add the single game tab and set visible
		getContentPane().remove(tabbedPane);
		getContentPane().add(tab);
		hmGameIdToTab.put(gameId, tab);
		setVisible(true);
	}
	public void initMatch(DartsMatchEntity match)
	{
		this.match = match;
		
		matchPanel.init(match);
		
		setScreenSize(match.getPlayerCount());
	}
	private void updateTotalScores()
	{
		matchPanel.updateTotalScores();
	}
	private DartsGamePanel<? extends DartsScorer> addGameToMatch(GameEntity game)
	{
		//Add to the match panel
		//matchPanel.addGame(game);
		
		//Cache this screen in ScreenCache
		long gameId = game.getRowId();
		ScreenCache.addDartsGameScreen(gameId, this);
		
		//Initialise some basic properties of the tab, such as visibility of components etc
		DartsGamePanel<? extends DartsScorer> tab = DartsGamePanel.factory(this, game.getGameType());
		tab.initBasic(game, match.getPlayerCount());
		
		//Add the single game tab and set the parent window to be visible
		tabbedPane.addTab("#" + gameId, tab);
		hmGameIdToTab.put(gameId, tab);
		setVisible(true);
		
		return tab;
	}
	private void setScreenSize(int playerCount)
	{
		int extraScorers = playerCount - 2;
		setSize(880 + (extraScorers * 180), 675 + (isMatch()?25:0));
		setResizable(false);
	}
	
	/**
	 * Update the match panel. Only do this if we're a match screen, otherwise we haven't initted the relevant table model
	 */
	public void addParticipant(long gameId, ParticipantEntity participant)
	{
		if (match != null)
		{
			matchPanel.addParticipant(gameId, participant);
		}
	}
	
	/**
	 * Fix for versions above 2.3.0. This will never have worked!
	 */
	/*public void fireAppearancePreferencesChanged()
	{
		for (int i=0; i<hmGameIdToTab.size(); i++)
		{
			DartsGamePanel<? extends DartsScorer> gameTab = hmGameIdToTab.get(i);
			gameTab.fireAppearancePreferencesChanged();
		}
	}*/
	public void fireAppearancePreferencesChanged()
	{
		HandyArrayList<Long> gameIds = hmGameIdToTab.getKeysAsVector();
		for (long gameId : gameIds)
		{
			DartsGamePanel<? extends DartsScorer> gameTab = hmGameIdToTab.get(gameId);
			gameTab.fireAppearancePreferencesChanged();
		}
	}
	
	public DartsGamePanel<? extends DartsScorer> getGamePanel()
	{
		if (isMatch())
		{
			Debug.stackTrace("Calling getGamePanel when this is a multi-game screen.");
		}
		
		HandyArrayList<DartsGamePanel<? extends DartsScorer>> tabs = hmGameIdToTab.getValuesAsVector();
		return tabs.firstElement();
	}
	
	/**
	 * Hook for when a GameId has been clicked and the screen is already visible. 
	 */
	public void displayGame(long gameId)
	{
		toFront();
		setState(NORMAL);
		
		if (isMatch())
		{
			DartsGamePanel<? extends DartsScorer> tab = hmGameIdToTab.get(gameId);
			tabbedPane.setSelectedComponent(tab);
		}
	}
	
	/**
	 * Called when the next game should start.
	 */
	public void startNextGameIfNecessary()
	{
		if (!isMatch())
		{
			return;
		}
		
		matchPanel.updateTotalScores();
		
		if (match.isComplete())
		{
			match.setDtFinish(DateUtil.getSqlDateNow());
			match.saveToDatabase();
			return;
		}
		
		//Factory and save the next game
		GameEntity nextGame = GameEntity.factoryAndSave(match);
		DartsGamePanel<? extends DartsScorer> panel = addGameToMatch(nextGame);
		
		//TODO - need to get the players off of the previous game (they're in the right order) then permute *those*
		//Otherwise loading doesn't work properly
		match.shufflePlayers();
		panel.startNewGame(match.getPlayers());
		
	}
	private boolean isMatch()
	{
		return match != null;
	}
	
	/**
	 * Static methods
	 */
	public static void launchNewGame(ArrayList<PlayerEntity> players, int gameType, String gameParams)
	{
		//Create and save a game
		GameEntity gameEntity = GameEntity.factoryAndSave(gameType, gameParams);
		
		//Construct the screen and factory a tab
		DartsGameScreen scrn = new DartsGameScreen();
		scrn.initSingleGame(gameEntity, players.size());
		
		//Now get the panel to start off a new game
		DartsGamePanel<? extends DartsScorer> panel = scrn.getGamePanel();
		panel.startNewGame(players);
	}
	public static void launchNewMatch(DartsMatchEntity match)
	{
		DartsGameScreen scrn = new DartsGameScreen();
		scrn.initMatch(match);
		
		GameEntity game = GameEntity.factoryAndSave(match);
		DartsGamePanel<? extends DartsScorer> panel = scrn.addGameToMatch(game);
		panel.startNewGame(match.getPlayers());
	}
	public static void loadAndDisplayGame(long gameId)
	{
		DartsGameScreen existingScreen = ScreenCache.getDartsGameScreen(gameId);
		if (existingScreen != null)
		{
			existingScreen.displayGame(gameId);
			return;
		}
		
		//Screen isn't currently visible, so look for the game on the DB
		GameEntity gameEntity = new GameEntity().retrieveForId(gameId, false);
		if (gameEntity == null)
		{
			DialogUtil.showError("Game #" + gameId + " does not exist.");
			return;
		}
		
		long matchId = gameEntity.getDartsMatchId();
		if (matchId == -1)
		{
			loadAndDisplaySingleGame(gameEntity);
		}
		else
		{
			loadAndDisplayMatch(matchId, gameId);
		}
	}
	private static void loadAndDisplaySingleGame(GameEntity gameEntity)
	{
		//We've found a game, so construct a screen and init it
		int playerCount = gameEntity.getParticipantCount();
		DartsGameScreen scrn = new DartsGameScreen();
		scrn.initSingleGame(gameEntity, playerCount);
		
		//Now try to load the game
		try
		{
			DartsGamePanel<? extends DartsScorer> panel = scrn.getGamePanel();
			panel.loadGame();
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			DialogUtil.showError("Failed to load Game #" + gameEntity.getRowId());
			scrn.dispose();
			ScreenCache.removeDartsGameScreen(scrn);
		}
	}
	private static void loadAndDisplayMatch(long matchId, long originalGameId)
	{
		HandyArrayList<GameEntity> allGames = GameEntity.retrieveGamesForMatch(matchId);
		
		//Get the last one so we can set the ordinal too. Oops.
		//GameEntity firstGame = allGames.get(0);
		GameEntity lastGame = allGames.lastElement();
		
		DartsMatchEntity match = new DartsMatchEntity().retrieveForId(matchId);
		match.cacheMetadataFromGame(lastGame);
		
		DartsGameScreen scrn = new DartsGameScreen();
		scrn.initMatch(match);
		
		try
		{
			for (int i=0; i<allGames.size(); i++)
			{
				GameEntity game = allGames.get(i);
				DartsGamePanel<? extends DartsScorer> panel = scrn.addGameToMatch(game);
				
				if (game.getRowId() == originalGameId)
				{
					panel.loadGame();
				}
				else
				{
					panel.preLoad();
				}
			}
			
			scrn.displayGame(originalGameId);
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			DialogUtil.showError("Failed to load Match #" + matchId);
			scrn.dispose();
			ScreenCache.removeDartsGameScreen(scrn);
		}
		
		scrn.updateTotalScores();
	}
	
	/**
	 * ChangeListener
	 */
	@Override
	public void stateChanged(ChangeEvent e)
	{
		JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
		
        Component selectedTab = sourceTabbedPane.getComponentAt(index);
        if (selectedTab instanceof DartsGamePanel)
        {
        	DartsGamePanel<? extends DartsScorer> panel = (DartsGamePanel<? extends DartsScorer>)selectedTab;
            String title = panel.getGameTitle();
            setTitle(title);
            
            if (panel.getPendingLoad())
            {
            	panel.loadGameInCatch();
            }
        }
        else
        {
        	setTitle(match.getMatchDesc());
        }
	}
	
	/**
	 * WindowListener
	 */
	@Override
	public void windowClosed(WindowEvent arg0)
	{
		ScreenCache.removeDartsGameScreen(this);
		closeResourcesOnExit();
	}
	private void closeResourcesOnExit()
	{
		HandyArrayList<DartsGamePanel<? extends DartsScorer>> tabs = hmGameIdToTab.getValuesAsVector();
		for (int i=0; i<tabs.size(); i++)
		{
			DartsGamePanel<? extends DartsScorer> tab = tabs.get(i);
			tab.closeResources();
		}
	}
	
	@Override
	public void windowActivated(WindowEvent arg0){}
	@Override
	public void windowClosing(WindowEvent arg0){}
	@Override
	public void windowDeactivated(WindowEvent arg0){}
	@Override
	public void windowDeiconified(WindowEvent arg0){}
	@Override
	public void windowIconified(WindowEvent arg0){}
	@Override
	public void windowOpened(WindowEvent arg0){}
}
