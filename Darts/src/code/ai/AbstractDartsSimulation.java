package code.ai;

import java.sql.Timestamp;

import code.listener.DartboardListener;
import code.object.Dart;
import code.screen.Dartboard;
import code.stats.GameWrapper;
import object.HandyArrayList;
import object.HashMapList;
import util.DateUtil;
import util.Debug;

public abstract class AbstractDartsSimulation implements DartboardListener
{
	protected boolean logging = false;
	
	protected AbstractDartsModel model = null;
	protected Dartboard dartboard = null;
	
	
	//Transient things
	protected Timestamp dtStart = null;
	protected Timestamp dtFinish = null;
	protected int currentRound = -1;
	protected HandyArrayList<Dart> dartsThrown = new HandyArrayList<>();
	
	protected HashMapList<Integer, Dart> hmRoundNumberToDarts = new HashMapList<>();
	
	public AbstractDartsSimulation(Dartboard dartboard, AbstractDartsModel model)
	{
		this.dartboard = dartboard;
		this.model = model;
		
		dartboard.addDartboardListener(this);
	}
	
	public abstract boolean shouldPlayCurrentRound();
	public abstract void startRound();
	public abstract int getTotalScore();
	public abstract String getGameParams();
	public abstract int getGameType();
	
	public GameWrapper simulateGame(long gameId)
	{
		resetVariables();
		
		dtStart = DateUtil.getSqlDateNow();
		
		while (shouldPlayCurrentRound())
		{
			startRound();
		}
		
		dtFinish = DateUtil.getSqlDateNow();
		
		int totalRounds = currentRound - 1;
		int totalScore = getTotalScore();
		
		Debug.appendBanner("Game Over. Rounds: " + totalRounds + ", Score: " + totalScore, logging);
		
		GameWrapper wrapper = new GameWrapper(gameId, getGameParams(), dtStart, dtFinish, totalScore);
		wrapper.setHmRoundNumberToDartsThrown(hmRoundNumberToDarts);
		wrapper.setTotalRounds(totalRounds);
		
		return wrapper;	
	}
	
	protected void resetVariables()
	{
		dartsThrown = new HandyArrayList<>();
		hmRoundNumberToDarts = new HashMapList<>();
		currentRound = 1;
	}
	protected void resetRound()
	{
		dartsThrown = new HandyArrayList<>();
		dartboard.clearDarts();
	}
	
	public void setLogging(boolean logging)
	{
		this.logging = logging;
	}
}
