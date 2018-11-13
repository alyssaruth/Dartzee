package code.screen.game;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import code.ai.AbstractDartsModel;
import code.bean.SliderAiSpeed;
import code.db.GameEntity;
import code.db.ParticipantEntity;
import code.db.PlayerEntity;
import code.db.RoundEntity;
import code.listener.DartboardListener;
import code.object.Dart;
import code.screen.Dartboard;
import code.stats.PlayerSummaryStats;
import code.utils.DartsRegistry;
import code.utils.DatabaseUtil;
import code.utils.PreferenceUtil;
import object.HandyArrayList;
import object.HashMapList;
import object.SuperHashMap;
import util.DateUtil;
import util.Debug;
import util.DialogUtil;

public abstract class DartsGamePanel<S extends DartsScorer> extends PanelWithScorers<S>
														  	implements DartboardListener,
														  			   ActionListener,
														  			   DartsRegistry
{
	protected static final boolean VERBOSE_LOGGING = false;
	
	protected SuperHashMap<Integer, ParticipantEntity> hmPlayerNumberToParticipant = new SuperHashMap<>();
	protected SuperHashMap<Integer, S> hmPlayerNumberToDartsScorer = new SuperHashMap<>();
	protected HashMap<Integer, Integer> hmPlayerNumberToLastRoundNumber = new HashMap<>();
	
	protected GameEntity gameEntity = null;
	protected int totalPlayers = -1;
	
	protected DartsGameScreen parentWindow = null;
	private String gameTitle = "";
	
	//If this tab is displaying as part of a loaded match, but this game still needs loading, this will be set.
	private boolean pendingLoad = false; 
	
	//Transitive things
	protected int currentPlayerNumber = 0;
	protected S activeScorer = null;
	protected HandyArrayList<Dart> dartsThrown = new HandyArrayList<>();
	protected RoundEntity currentRound = null;
	
	//For AI turns
	private Timer cpuTurn = null;
	
	public DartsGamePanel(DartsGameScreen parent)
	{
		super();
		scorerEast.tableScores.setToolTipText("View Stats");
		
		this.parentWindow = parent;
		
		panelCenter.add(dartboard, BorderLayout.CENTER);
		dartboard.addDartboardListener(this);
		panelCenter.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new BorderLayout(0, 0));
		slider.setValue(1000);
		slider.setSize(new Dimension(100, 200));
		slider.setPreferredSize(new Dimension(40, 200));
		panelSouth.add(panelButtons, BorderLayout.SOUTH);
		btnConfirm.setPreferredSize(new Dimension(80, 80));
		btnConfirm.setIcon(new ImageIcon(DartsGamePanel.class.getResource("/buttons/Confirm.png")));
		btnConfirm.setToolTipText("Confirm round");
		panelButtons.add(btnConfirm);
		btnReset.setPreferredSize(new Dimension(80, 80));
		btnReset.setIcon(new ImageIcon(DartsGamePanel.class.getResource("/buttons/Reset.png")));
		btnReset.setToolTipText("Reset round");
		panelButtons.add(btnReset);
		btnStats.setToolTipText("View stats");
		btnStats.setPreferredSize(new Dimension(80, 80));
		btnStats.setIcon(new ImageIcon(DartsGamePanel.class.getResource("/buttons/stats.png")));
		
		panelButtons.add(btnStats);
		btnSlider.setToolTipText("AI throw speed");
		btnSlider.setPreferredSize(new Dimension(80, 80));
		
		slider.setOrientation(SwingConstants.VERTICAL);
		slider.setVisible(false);
		
		panelButtons.add(btnSlider);
		
		btnConfirm.addActionListener(this);
		btnReset.addActionListener(this);
		btnStats.addActionListener(this);
		btnSlider.addActionListener(this);
		
		if (statsPanel == null)
		{
			btnStats.setVisible(false);
		}
		
		dartboard.setRenderScoreLabels(true);
	}
	
	/**
	 * Screen stuff
	 */
	protected final Dartboard dartboard = new Dartboard();
	protected final GameStatisticsPanel statsPanel = factoryStatsPanel();
	
	private final JPanel panelSouth = new JPanel();
	protected final SliderAiSpeed slider = new SliderAiSpeed(true);
	private final JPanel panelButtons = new JPanel();
	private final JButton btnConfirm = new JButton("");
	private final JButton btnReset = new JButton("");
	private final JToggleButton btnStats = new JToggleButton("");
	private final JToggleButton btnSlider = new JToggleButton("");
	
	
	/**
	 * Abstract methods
	 */
	public abstract void doAiTurn(AbstractDartsModel model);
	public abstract void loadDartsForParticipant(int playerNumber, HashMapList<Integer, Dart> hmRoundToDarts, int totalRounds);
	public abstract void updateVariablesForNewRound();
	public abstract void resetRoundVariables();
	public abstract void updateVariablesForDartThrown(Dart dart);
	public abstract boolean shouldStopAfterDartThrown();
	public abstract boolean shouldAIStop();
	public abstract void saveDartsAndProceed();
	public abstract void initImpl(String gameParams);
	public abstract GameStatisticsPanel factoryStatsPanel();
	
	/**
	 * Regular methods
	 */
	public void startNewGame(ArrayList<PlayerEntity> players)
	{
		for (int i=0; i<totalPlayers; i++)
		{
			PlayerEntity player = players.get(i);
	
			long gameId = gameEntity.getRowId();
			ParticipantEntity participant = ParticipantEntity.factoryAndSave(gameId, player, i);
			addParticipant(i, participant);
			
			assignScorer(player, i);
		}
		
		initForAi(hasAi());
		dartboard.paintDartboard();
		
		nextTurn();
	}
	protected void nextTurn()
	{
		activeScorer = hmPlayerNumberToDartsScorer.get(currentPlayerNumber);
		selectScorer(activeScorer);
		
		dartsThrown.clear();
		
		updateVariablesForNewRound();
		
		ParticipantEntity participant = hmPlayerNumberToParticipant.get(currentPlayerNumber);
		int lastRoundForThisPlayer = getLastRoundNumber();
		
		//Create a new round for this player
		int newRoundNo = lastRoundForThisPlayer+1;
		currentRound = RoundEntity.factory(participant, newRoundNo);
		hmPlayerNumberToLastRoundNumber.put(currentPlayerNumber, newRoundNo);
		
		Debug.appendBanner(activeScorer.getPlayerName() + ": Round " + newRoundNo, VERBOSE_LOGGING);
		
		btnReset.setEnabled(false);
		btnConfirm.setEnabled(false);
		
		btnStats.setEnabled(newRoundNo > 1);
		
		readyForThrow();
	}
	private int getLastRoundNumber()
	{
		Integer lastRound = hmPlayerNumberToLastRoundNumber.get(currentPlayerNumber);
		if (lastRound == null)
		{
			return 0;
		}
		
		return lastRound.intValue();
	}
	private void selectScorer(S selectedScorer)
	{
		for (S scorer : scorersOrdered)
		{
			scorer.setSelected(false);
		}
		
		selectedScorer.setSelected(true);
	}
	
	private void assignScorer(PlayerEntity player, int playerNumber)
	{
		assignScorer(player, hmPlayerNumberToDartsScorer, playerNumber, gameEntity.getGameParams());
	}
	private void initForAi(boolean hasAi)
	{
		dartboard.addOverlay(new Point(329, 350), slider);
		btnSlider.setVisible(hasAi);
		
		int defaultSpd = PreferenceUtil.getIntValue(PREFERENCES_INT_AI_SPEED);
		slider.setValue(defaultSpd);
	}
	
	
	public void initBasic(GameEntity gameEntity, int totalPlayers)
	{
		this.gameEntity = gameEntity;
		this.totalPlayers = totalPlayers;
		
		cpuTurn = new Timer("Timer-CpuTurn #" + gameEntity.getRowId());
		
		long gameNo = gameEntity.getRowId();
		String gameDesc = gameEntity.getTypeDesc();
		gameTitle = "Game #" + gameNo + " (" + gameDesc + ", " + totalPlayers + " players)";
		
		if (statsPanel != null)
		{
			statsPanel.setGameParams(gameEntity.getGameParams());
		}
		
		initScorers(totalPlayers);
		
		initImpl(gameEntity.getGameParams());
	}
	
	public void loadGameInCatch()
	{
		try
		{
			loadGame();
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			
			if (gameEntity != null)
			{
				DialogUtil.showError("Failed to load Game #" + gameEntity.getRowId());
			}
			else
			{
				DialogUtil.showError("Failed to load game.");
			}
		}
	}
	
	/**
	 * Called when loading up a match for the tabs that aren't visible. Just do enough so that we can generate the match 
	 * summary, and set a flag to say this tab needs to do a proper load if selected.
	 */
	public void preLoad()
	{
		long gameId = gameEntity.getRowId();
		loadParticipants(gameId);
		
		pendingLoad = true;
	}
	public void loadGame() throws Throwable
	{
		pendingLoad = false;
		
		long gameId = gameEntity.getRowId();
		
		//Get the participants, sorted by Ordinal. Assign their scorers.
		loadParticipants(gameId);
		loadScoresAndCurrentPlayer(gameId);
		
		//Paint the dartboard
		dartboard.paintDartboard();
		
		//If the game is over, do some extra stuff to sort the screen out
		Timestamp dtFinish = gameEntity.getDtFinish();
		if (!DateUtil.isEndOfTime(dtFinish))
		{
			setGameReadOnly();
		}
		else
		{
			nextTurn();
		}
	}
	private void setGameReadOnly()
	{
		dartboard.stopListening();
		
		if (getActiveCount() == 0)
		{
			btnSlider.setVisible(false);
			btnConfirm.setVisible(false);
			btnReset.setVisible(false);
		}
		else
		{
			slider.setEnabled(false);
			btnConfirm.setEnabled(false);
			btnReset.setEnabled(false);
		}
		
		//Default to showing the stats panel for completed games, if applicable
		if (btnStats.isVisible())
		{
			btnStats.setSelected(true);
			viewStats();
		}
			
		updateScorersWithFinishingPositions();
	}
	protected void updateScorersWithFinishingPositions()
	{
		HandyArrayList<Integer> players = hmPlayerNumberToDartsScorer.getKeysAsVector();
		for (int playerNumber : players)
		{
			S scorer = hmPlayerNumberToDartsScorer.get(playerNumber);
			ParticipantEntity pt = hmPlayerNumberToParticipant.get(playerNumber);
			
			scorer.updateResultColourForPosition(pt.getFinishingPosition());
		}
	}
	
	/**
	 * Retrieve the ordered participants and assign their scorers
	 */
	private void loadParticipants(long gameId)
	{
		//We may have already done this in the preLoad
		if (!hmPlayerNumberToParticipant.isEmpty())
		{
			return;
		}
		
		String whereSql = "GameId = " + gameId + " ORDER BY Ordinal ASC";
		ArrayList<ParticipantEntity> participants = new ParticipantEntity().retrieveEntities(whereSql);
		
		for (int i=0; i<participants.size(); i++)
		{
			ParticipantEntity pt = participants.get(i);
			addParticipant(i, pt);
			
			PlayerEntity player = pt.getPlayer();
			assignScorer(player, i);
		}
		
		initForAi(hasAi());
	}
	
	/**
	 * Populate the scorers and populate the current player by:
	 * 
	 *  - Finding the Max(RoundNumber) for this game
	 *  - Finding how many players have already completed this round, X.
	 *  - CurrentPlayerNumber = X % totalPlayers
	 */
	private void loadScoresAndCurrentPlayer(long gameId) throws SQLException
	{
		int maxRounds = 0;
		
		for (int i=0; i<totalPlayers; i++)
		{
			ParticipantEntity pt = hmPlayerNumberToParticipant.get(i);
			String sql = "SELECT rnd.RoundNumber, drt.Score, drt.Multiplier, drt.PosX, drt.PosY, drt.SegmentType"
					   + " FROM Round rnd, Dart drt"
					   + " WHERE drt.RoundId = rnd.RowId"
					   + " AND rnd.ParticipantId = " + pt.getRowId()
					   + " ORDER BY rnd.RoundNumber, drt.Ordinal";
			
			HashMapList<Integer, Dart> hmRoundToDarts = new HashMapList<>();
			int lastRound = 0;
			
			try (ResultSet rs = DatabaseUtil.executeQuery(sql))
			{
				while (rs.next())
				{
					int roundNumber = rs.getInt(1);
					int score = rs.getInt("Score");
					int multiplier = rs.getInt("Multiplier");
					int posX = rs.getInt("PosX");
					int posY = rs.getInt("PosY");
					int segmentType = rs.getInt("SegmentType");
					
					Dart drt = new Dart(new Point(posX, posY), score, multiplier, segmentType);
					hmRoundToDarts.putInList(roundNumber, drt);
					
					lastRound = roundNumber;
				}
			}
			catch (SQLException sqle)
			{
				Debug.logSqlException(sql, sqle);
				throw sqle;
			}
			
			loadDartsForParticipant(i, hmRoundToDarts, lastRound);
			
			hmPlayerNumberToLastRoundNumber.put(i, lastRound);
			
			maxRounds = Math.max(maxRounds, lastRound);
		}
		
		setCurrentPlayer(maxRounds, gameId);
	}
	
	/**
	 * 1) Get the MAX(Ordinal) of the person who's played the maxRounds, i.e. the last player to have a turn.
	 * 2) Call into getNextPlayer(), which takes into account inactive players.
	 */
	private void setCurrentPlayer(int maxRounds, long gameId)
	{
		if (maxRounds == 0)
		{
			//The game literally hasn't started yet. No one has completed a round. 
			Debug.append("MaxRounds = 0, so setting CurrentPlayerNumber = 0 as game hasn't started.");
			currentPlayerNumber = 0;
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT MAX(pt.Ordinal) ");
		sb.append(" FROM Round rnd, Participant pt");
		sb.append(" WHERE rnd.ParticipantId = pt.RowId");
		sb.append(" AND pt.GameId = ");
		sb.append(gameId);
		sb.append(" AND rnd.RoundNumber = ");
		sb.append(maxRounds);
		
		int lastPlayerNumber = DatabaseUtil.executeQueryAggregate(sb);
		currentPlayerNumber = getNextPlayerNumber(lastPlayerNumber);
		
		Debug.append("MaxRounds = " + maxRounds + ", CurrentPlayerNumber = " + currentPlayerNumber); 
	}
	
	/**
	 * AI stuff
	 */
	public void scheduleAiTurn(int delay)
	{
		try
		{
			cpuTurn.schedule(new DelayedOpponentTurn(), delay);
		}
		catch (IllegalStateException ise)
		{
			//Do nothing - if we're catching this it's because we cancelled the timer, which is because
			//we closed the game screen down.
		}
	}
	public void allPlayersFinished()
	{
		Debug.append("All players now finished.", VERBOSE_LOGGING);
		
		dartboard.stopListening();
		
		ArrayList<ParticipantEntity> participants = hmPlayerNumberToParticipant.getValuesAsVector();
		for (ParticipantEntity pt : participants)
		{
			long playerId = pt.getPlayerId();
			PlayerSummaryStats.resetPlayerStats(playerId, gameEntity.getGameType());
		}
	}
	
	/**
	 * Should I stop throwing?
	 * 
	 * Default behaviour for if window has been closed, with extensible hook (e.g. in X01 where an AI can be paused).
	 */
	private boolean shouldStopThrowing()
	{
		if (!parentWindow.isVisible())
		{
			Debug.append("Game window has been closed, stopping throwing.");
			return true;
		}
		
		return shouldAIStop();
	}
	
	protected int getNextPlayerNumber(int currentPlayerNumber)
	{
		if (getActiveCount() == 0)
		{
			return -1;
		}
		
		int candidate = (currentPlayerNumber + 1) % totalPlayers;
		while (!isActive(candidate))
		{
			candidate = (candidate + 1) % totalPlayers;
		}
		
		return candidate;
	}
	
	private boolean hasAi()
	{
		ArrayList<ParticipantEntity> ptcpts = hmPlayerNumberToParticipant.getValuesAsVector();
		for (int i=0; i<ptcpts.size(); i++)
		{
			ParticipantEntity pe = ptcpts.get(i);
			if (pe.isAi())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public int getActiveCount()
	{
		int count = 0;
		
		ArrayList<ParticipantEntity> participants = hmPlayerNumberToParticipant.getValuesAsVector();
		for (int i=0; i<participants.size(); i++)
		{
			ParticipantEntity participant = participants.get(i);
			if (participant.isActive())
			{
				count++;
			}
		}
		
		return count;
	}
	private boolean isActive(int playerNumber)
	{
		ParticipantEntity participant = hmPlayerNumberToParticipant.get(playerNumber);
		return participant.isActive();
	}
	
	public void fireAppearancePreferencesChanged()
	{
		//Don't repaint the dartboard because this clears out all of the cached segments etc.
		//So if an AI is in progress as you change appearance preferences, things fuck up.
		//dartboard.paintDartboard();
		
		for (S scorer : scorersOrdered)
		{
			scorer.repaint();
		}
	}
	
	public long getGameId()
	{
		if (gameEntity == null)
		{
			return -1;
		}
		
		return gameEntity.getRowId();
	}
	
	protected int handlePlayerFinish()
	{
		ParticipantEntity participant = hmPlayerNumberToParticipant.get(currentPlayerNumber);
		
		int finishingPosition = getFinishingPositionFromPlayersRemaining();
		int numberOfDarts = activeScorer.getTotalScore();
		
		participant.setFinishingPosition(finishingPosition);
		participant.setFinalScore(numberOfDarts);
		participant.setDtFinished(DateUtil.getSqlDateNow());
		participant.saveToDatabase();
		
		long playerId = participant.getPlayerId();
		PlayerSummaryStats.resetPlayerStats(playerId, gameEntity.getGameType());
	
		return finishingPosition;
	}
	
	protected int getFinishingPositionFromPlayersRemaining()
	{
		int playersLeft = getActiveCount();
		return totalPlayers - playersLeft + 1;
	}
	
	public static DartsGamePanel<? extends DartsScorer> factory(DartsGameScreen parent, int gameType)
	{
		if (gameType == GameEntity.GAME_TYPE_X01)
		{
			return new GamePanelX01(parent);
		}
		else if (gameType == GameEntity.GAME_TYPE_GOLF)
		{
			return new GamePanelGolf(parent);
		}
		else if (gameType == GameEntity.GAME_TYPE_ROUND_THE_CLOCK)
		{
			return new GamePanelRoundTheClock(parent);
		}
		
		Debug.stackTrace("Unexpected gameType: " + gameType);
		return null;
	}
	
	@Override
	public void dartThrown(Dart dart)
	{
		Debug.append("Hit " + dart, VERBOSE_LOGGING);
		
		dartsThrown.add(dart);
		activeScorer.addDart(dart);
		
		doAnimations(dart);
		
		//If there are any specific variables we need to update (e.g. current score for X01), do it now
		updateVariablesForDartThrown(dart);
		
		//Enable both of these
		btnReset.setEnabled(activeScorer.getHuman());
		if (!mustContinueThrowing())
		{
			btnConfirm.setEnabled(activeScorer.getHuman());
		}
		
		//If we've thrown three or should stop for other reasons (bust in X01), then stop throwing
		if (shouldStopAfterDartThrown())
		{
			stopThrowing();
		}
		else
		{
			//Fine, just carry on
			readyForThrow();
		}
	}
	private void doAnimations(Dart dart)
	{
		if (dart.getMultiplier() == 0)
		{
			doMissAnimation();
		}
		else if (dart.getTotal() == 50)
		{
			dartboard.doBull();
		}
	}
	protected void doMissAnimation()
	{
		dartboard.doBadMiss();
	}
	protected void stopThrowing()
	{
		if (activeScorer.getHuman())
		{
			dartboard.stopListening();
		}
		else
		{
			//No need to wait for confirmation, the turn is over
			try {Thread.sleep(slider.getValue());}catch(Throwable t){}
			confirmRound();
		}
	}
	private void confirmRound()
	{
		btnConfirm.setEnabled(false);
		btnReset.setEnabled(false);
		
		dartboard.clearDarts();
		activeScorer.confirmCurrentRound();
		
		currentRound.saveToDatabase();
		
		saveDartsAndProceed();
		
		
	}
	protected void resetRound()
	{
		Debug.append("Reset pressed.", VERBOSE_LOGGING);
		
		resetRoundVariables();
		
		dartboard.clearDarts();
		activeScorer.clearCurrentRound();
		activeScorer.updatePlayerResult();
		dartsThrown.clear();
		
		//If we're resetting, disable the buttons
		btnConfirm.setEnabled(false);
		btnReset.setEnabled(false);
		
		//Might need to re-enable the dartboard for listening if we're a human player
		boolean human = activeScorer.getHuman();
		dartboard.listen(human);
	}
	
	protected void readyForThrow()
	{
		if (activeScorer.getHuman())
		{
			//Human player
			dartboard.ensureListening();
		}
		else
		{
			//AI
			dartboard.stopListening();
			
			int sliderValue = slider.getValue();
			scheduleAiTurn(sliderValue);
		}
	}
	protected boolean mustContinueThrowing()
	{
		return false;
	}
	
	protected AbstractDartsModel getCurrentPlayerStrategy()
	{
		ParticipantEntity participant = hmPlayerNumberToParticipant.get(currentPlayerNumber);
		if (!participant.isAi())
		{
			Debug.stackTrace("Trying to get current strategy for human player: " + participant);
			return null;
		}
		
		return participant.getModel();
	}
	
	public int getTotalPlayers()
	{
		return totalPlayers;
	}
	public String getGameTitle()
	{
		return gameTitle;
	}
	public boolean getPendingLoad()
	{
		return pendingLoad;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		Object source = arg0.getSource();
		if (source != btnSlider)
		{
			btnSlider.setSelected(false);
			slider.setVisible(false);
		}
		
		if (source == btnReset)
		{
			resetRound();
		}
		else if (source == btnConfirm)
		{
			confirmRound();
		}
		else if (source == btnStats)
		{
			viewStats();
		}
		else if (source == btnSlider)
		{
			toggleSlider();
		}
	}
	
	private void toggleSlider()
	{
		slider.setVisible(btnSlider.isSelected());
		
		if (btnStats.isSelected())
		{
			btnStats.setSelected(false);
			viewStats();
		}
	}
	
	private void viewStats()
	{
		if (btnStats.isSelected())
		{
			panelCenter.remove(dartboard);
			panelCenter.add(statsPanel, BorderLayout.CENTER);
			
			statsPanel.showStats(getOrderedParticipants());
		}
		else
		{
			panelCenter.remove(statsPanel);
			panelCenter.add(dartboard, BorderLayout.CENTER);
		}
		
		panelCenter.revalidate();
		panelCenter.repaint();
	}
	
	private HandyArrayList<ParticipantEntity> getOrderedParticipants()
	{
		HandyArrayList<ParticipantEntity> participants = new HandyArrayList<>();
		
		for (int i=0; i<4; i++)
		{
			ParticipantEntity pt = hmPlayerNumberToParticipant.get(i);
			if (pt != null)
			{
				participants.add(pt);
			}
		}
		
		return participants;
	}
	
	private void addParticipant(int playerNumber, ParticipantEntity participant)
	{
		hmPlayerNumberToParticipant.put(playerNumber, participant);
		
		parentWindow.addParticipant(gameEntity.getRowId(), participant);
	}
	
	public void closeResources()
	{
		cpuTurn.cancel();
	}
	
	class DelayedOpponentTurn extends TimerTask
	{
		@Override
		public void run()
		{
			if (shouldStopThrowing())
			{
				return;
			}
			
			AbstractDartsModel model = getCurrentPlayerStrategy();
			doAiTurn(model);
		}
	}
}
