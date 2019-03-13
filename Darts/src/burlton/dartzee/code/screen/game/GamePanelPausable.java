package burlton.dartzee.code.screen.game;

import burlton.dartzee.code.db.ParticipantEntity;
import burlton.dartzee.code.utils.PreferenceUtil;
import burlton.desktopcore.code.util.DateUtil;
import burlton.core.code.util.Debug;

import static burlton.dartzee.code.utils.RegistryConstantsKt.PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE;

public abstract class GamePanelPausable<S extends DartsScorerPausable> extends DartsGamePanel<S>
{
	private Object AI_PAUSE_SYNC_OBJ = new Object();
	private boolean aiShouldPause = false;
	
	public GamePanelPausable(DartsGameScreen parent)
	{
		super(parent);
	}
	
	/**
	 * Abstract methods
	 */
	public abstract void saveDartsToDatabase(long roundId);
	public abstract boolean currentPlayerHasFinished();
	
	@Override
	public void saveDartsAndProceed()
	{
		activeScorer.updatePlayerResult();
		
		long roundId = currentRound.getRowId();
		saveDartsToDatabase(roundId);
		
		//This player has finished. The game isn't necessarily over though...
		if (currentPlayerHasFinished())
		{
			handlePlayerFinish();
		}
		
		currentPlayerNumber = getNextPlayerNumber(currentPlayerNumber);
		
		int activePlayers = getActiveCount();
		if (activePlayers > 1)
		{
			//We always keep going if there's more than 1 active person in play
			nextTurn();
		}
		else if (activePlayers == 1)
		{
			activeScorer = hmPlayerNumberToDartsScorer.get(currentPlayerNumber);
			
			//Finish the game and set the last player's finishing position if we haven't already
			finishGameIfNecessary();
			
			if (!activeScorer.getPaused())
			{
				nextTurn();
			}
		}
		else
		{
			notifyAiPaused();
			allPlayersFinished();
		}
	}
	
	@Override
	protected int handlePlayerFinish()
	{
		int finishPos = super.handlePlayerFinish();
		activeScorer.finalisePlayerResult(finishPos);
		return finishPos;
	}
	
	@Override
	public boolean shouldAIStop()
	{
		if (aiShouldPause)
		{
			Debug.append("Been told to pause, stopping throwing.");
			aiShouldPause = false;
			
			notifyAiPaused();
			
			return true;
		}
		
		return false;
	}
	
	protected void finishGameIfNecessary()
	{
		if (gameEntity.isFinished())
		{
			return;
		}
		
		ParticipantEntity loser = hmPlayerNumberToParticipant.get(currentPlayerNumber);
		loser.setFinishingPosition(totalPlayers);
		loser.saveToDatabase();
		
		gameEntity.setDtFinish(DateUtil.getSqlDateNow());
		gameEntity.saveToDatabase();
		
		parentWindow.startNextGameIfNecessary();
		
		//Display this player's result. If they're an AI and we have the preference, then
		//automatically play on.
		activeScorer.finalisePlayerResult(totalPlayers);
		if (loser.isAi()
		  && PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE))
		{
			activeScorer.toggleResume();
		}
	}
	
	public void pauseLastPlayer()
	{
		if (!activeScorer.getHuman())
		{
			synchronized (AI_PAUSE_SYNC_OBJ)
			{
				try 
				{ 
					aiShouldPause = true;
					AI_PAUSE_SYNC_OBJ.wait(); 
				} 
				catch (InterruptedException ie) 
				{
					Debug.stackTrace(ie);
				}
			}
		}
		
		//Now the player has definitely stopped, reset the round
		resetRound();
		dartboard.stopListening();
	}
	
	public void unpauseLastPlayer()
	{
		//If we've come through game load, we'll have disabled this.
		slider.setEnabled(true);
		
		ParticipantEntity pt = hmPlayerNumberToParticipant.get(currentPlayerNumber);
		if (currentRound == null
	      || !currentRound.isForParticipant(pt))
		{
			//We need to create a new Round entity. Either we've come through game load or it's the first time we've pressed unpause.
			nextTurn();
		}
		else
		{
			//Don't do nextTurn() as that will up the round number when we don't want it to be upped
			readyForThrow();
		}
	}
	
	/**
	 * When we click the pause button, the EDT is frozen while we wait for the AI to actually pause.
	 * At this point, one of two things will happen. Both will result in us notifying to wake the EDT back up.
	 * 
	 *  - The AI will come to begin a throw and find that it should stop.
	 *  - The AI was throwing its last dart as pause was pressed, so won't hit the above code. 
	 *    For this reason, we also notify when the last player totally finishes, *after* the point that the
	 *    pause/unpause button has been removed from the screen.
	 */
	protected void notifyAiPaused()
	{
		synchronized (AI_PAUSE_SYNC_OBJ)
		{
			AI_PAUSE_SYNC_OBJ.notifyAll();
		}
	}
}
