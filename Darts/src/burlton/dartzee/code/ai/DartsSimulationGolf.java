package burlton.dartzee.code.ai;

import burlton.core.code.util.Debug;
import burlton.dartzee.code.db.GameEntityKt;
import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.screen.Dartboard;

public final class DartsSimulationGolf extends AbstractDartsSimulation
{
	private static final int ROUNDS = 18;
	
	private int score = 0;
	
	public DartsSimulationGolf(Dartboard dartboard, AbstractDartsModel model)
	{
		super(dartboard, model);
	}

	@Override
	public boolean shouldPlayCurrentRound()
	{
		return currentRound <= ROUNDS;
	}
	
	@Override
	protected void resetVariables()
	{
		super.resetVariables();
		score = 0;
	}
	
	@Override
	public void startRound()
	{
		resetRound();
		
		int dartNo = dartsThrown.size() + 1;
		model.throwGolfDart(currentRound, dartNo, dartboard);
	}
	
	private void finishedRound()
	{
		hmRoundNumberToDarts.put(currentRound, dartsThrown);
		
		Dart drt = dartsThrown.lastElement();
		int roundScore = drt.getGolfScore(currentRound);
		score += roundScore;
		
		Debug.appendBanner("Round " + currentRound, logging);
		Debug.append("Darts [" + dartsThrown + "]", logging);
		Debug.append("Score [" + roundScore + "]", logging);
		Debug.append("Total Score [" + score + "]", logging);
		
		currentRound++;
	}
	
	@Override
	public void dartThrown(Dart dart)
	{
		dartsThrown.add(dart);
		
		int noDarts = dartsThrown.size();
		int stopThreshold = model.getStopThresholdForDartNo(noDarts);
		
		if (noDarts == 3
		 || dart.getGolfScore(currentRound) <= stopThreshold)
		{
			finishedRound();
		}
		else
		{
			int dartNo = dartsThrown.size() + 1;
			model.throwGolfDart(currentRound, dartNo, dartboard);
		}
	}

	@Override
	public int getTotalScore()
	{
		return score;
	}

	@Override
	public String getGameParams()
	{
		return "" + ROUNDS;
	}
	
	@Override
	public int getGameType()
	{
		return GameEntityKt.GAME_TYPE_GOLF;
	}

}
