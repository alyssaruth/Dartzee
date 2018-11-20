package code.screen.game;

import code.ai.AbstractDartsModel;
import code.db.DartEntity;
import code.db.ParticipantEntity;
import code.object.Dart;
import object.HandyArrayList;
import object.HashMapList;

public final class GamePanelRoundTheClock extends GamePanelPausable<DartsScorerRoundTheClock>
{	
	private String clockType = "";

	public GamePanelRoundTheClock(DartsGameScreen parent)
	{
		super(parent);
	}

	@Override
	public void doAiTurn(AbstractDartsModel model)
	{
		int currentTarget = activeScorer.getCurrentClockTarget();
		model.throwClockDart(currentTarget, clockType, dartboard);
	}

	@Override
	public void loadDartsForParticipant(int playerNumber, HashMapList<Integer, Dart> hmRoundToDarts, int lastRound)
	{
		DartsScorerRoundTheClock scorer = hmPlayerNumberToDartsScorer.get(playerNumber);
		for (int i=1; i<=lastRound; i++)
		{
			HandyArrayList<Dart> darts = hmRoundToDarts.get(i);
			addDartsToScorer(darts, scorer);
		}
		
		ParticipantEntity pt = hmPlayerNumberToParticipant.get(playerNumber);
		int finishPos = pt.getFinishingPosition();
		if (finishPos > -1)
		{
			scorer.finalisePlayerResult(finishPos);
		}
	}
	private void addDartsToScorer(HandyArrayList<Dart> darts, DartsScorerRoundTheClock scorer)
	{
		int clockTarget = scorer.getCurrentClockTarget();
		
		for (Dart dart : darts)
		{
			dart.setStartingScore(clockTarget);
			scorer.addDart(dart);
			
			if (dart.hitClockTarget(clockType))
			{
				scorer.incrementCurrentClockTarget();
				clockTarget = scorer.getCurrentClockTarget();
			}
		}
		
		//Need to take brucey into account
		if (darts.size() < 4)
		{
			scorer.disableBrucey();
		}
		
		scorer.confirmCurrentRound();
	}

	@Override
	public void updateVariablesForNewRound(){}

	@Override
	public void resetRoundVariables(){}

	@Override
	public void updateVariablesForDartThrown(Dart dart)
	{
		int currentClockTarget = activeScorer.getCurrentClockTarget();
		dart.setStartingScore(currentClockTarget);
		
		if (dart.hitClockTarget(clockType))
		{
			activeScorer.incrementCurrentClockTarget();
		
			if (dartsThrown.size() == 4)
			{
				dartboard.doForsyth();
			}
		}
		else if (!(dartsThrown.size() == 4))
		{
			activeScorer.disableBrucey();
		}
	}

	@Override
	public boolean shouldStopAfterDartThrown()
	{
		if (dartsThrown.size() == 4)
		{
			return true;
		}
		
		if (activeScorer.getCurrentClockTarget() > 20)
		{
			//Finished.
			return true;
		}
		
		boolean allHits = true;
		for (Dart dart : dartsThrown)
		{
			allHits &= dart.hitClockTarget(clockType);
		}
		
		if (dartsThrown.size() == 3
		  && !allHits)
		{
			//No brucey bonus
			return true;
		}
		
		return false;
	}
	
	@Override
	protected boolean mustContinueThrowing()
	{
		return !shouldStopAfterDartThrown();
	}
	
	@Override
	public void saveDartsToDatabase(long roundId)
	{
		for (int i=0; i<dartsThrown.size(); i++)
		{
			Dart dart = dartsThrown.get(i);
			int target = dart.getStartingScore();
			DartEntity.factoryAndSave(dart, roundId, i+1, target);
		}
	}
	
	@Override
	public boolean currentPlayerHasFinished()
	{
		return activeScorer.getCurrentClockTarget() > 20;
	}

	@Override
	public void initImpl(String gameParams)
	{
		this.clockType = gameParams;
	}

	@Override
	public DartsScorerRoundTheClock factoryScorer()
	{
		DartsScorerRoundTheClock scorer = new DartsScorerRoundTheClock();
		scorer.setParent(this);
		return scorer;
	}

	@Override
	public GameStatisticsPanel factoryStatsPanel()
	{
		return new GameStatisticsPanelRoundTheClock();
	}

}
