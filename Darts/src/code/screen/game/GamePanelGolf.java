package code.screen.game;

import code.ai.AbstractDartsModel;
import code.db.DartEntity;
import code.db.ParticipantEntity;
import code.object.Dart;
import object.HandyArrayList;
import object.HashMapList;
import util.DateUtil;

public class GamePanelGolf extends DartsGamePanel<DartsScorerGolf>
{
	//Number of rounds - 9 holes or 18?
	private int numberOfRounds = -1;
	
	public GamePanelGolf(DartsGameScreen parent)
	{
		super(parent);
	}
	
	@Override
	public void initImpl(String gameParams)
	{
		//The params tell us how many holes
		numberOfRounds = Integer.parseInt(gameParams);
	}
	
	@Override
	public void doAiTurn(AbstractDartsModel model)
	{
		int targetHole = currentRound.getRoundNumber();
		int dartNo = dartsThrown.size() + 1;
		model.throwGolfDart(targetHole, dartNo, dartboard);
	}

	@Override
	public void loadDartsForParticipant(int playerNumber, HashMapList<Integer, Dart> hmRoundToDarts, int lastRound)
	{
		DartsScorerGolf scorer = hmPlayerNumberToDartsScorer.get(playerNumber);
		for (int i=1; i<=lastRound; i++)
		{
			HandyArrayList<Dart> darts = hmRoundToDarts.get(i);
			scorer.addDarts(darts);
		}
	}

	@Override
	public void updateVariablesForNewRound(){}
	@Override
	public void resetRoundVariables(){}
	@Override
	public void updateVariablesForDartThrown(Dart dart){}

	@Override
	public boolean shouldStopAfterDartThrown()
	{
		if (dartsThrown.size() == 3)
		{
			return true;
		}
		
		int score = getScoreForMostRecentDart();
		if (activeScorer.getHuman())
		{
			return score == 1;
		}
		else
		{
			int noDarts = dartsThrown.size();
			
			AbstractDartsModel model = getCurrentPlayerStrategy();
			int stopThreshold = model.getStopThresholdForDartNo(noDarts);

			return score <= stopThreshold;
		}
	}
	private int getScoreForMostRecentDart()
	{
		Dart lastDart = dartsThrown.lastElement();
		
		int targetHole = currentRound.getRoundNumber();
		return lastDart.getGolfScore(targetHole);
	}

	@Override
	public void saveDartsAndProceed()
	{
		long roundId = currentRound.getRowId();
		for (int i=0; i<dartsThrown.size(); i++)
		{
			Dart dart = dartsThrown.get(i);
			DartEntity.factoryAndSave(dart, roundId, i+1, -1);
		}
		
		activeScorer.finaliseRoundScore();
		
		if (currentRound.getRoundNumber() == numberOfRounds)
		{
			handlePlayerFinish();
		}
		
		currentPlayerNumber = getNextPlayerNumber(currentPlayerNumber);
		if (getActiveCount() > 0)
		{
			nextTurn();
		}
		else
		{
			finishGame();
		}
	}
	
	@Override
	protected int getFinishingPositionFromPlayersRemaining()
	{
		//Finishing positions are determined at the end
		return -1;
	}
	private void finishGame()
	{
		//Get the participants sorted by score so we can assign finishing positions
		HandyArrayList<ParticipantEntity> participants = hmPlayerNumberToParticipant.getValuesAsVector();
		participants.sort((ParticipantEntity p1, ParticipantEntity p2) -> Integer.compare(p1.getFinalScore(), p2.getFinalScore()));
		
		int previousScore = Integer.MAX_VALUE;
		int finishPos = 1;
		for (int i=0; i<participants.size(); i++)
		{
			ParticipantEntity pt = participants.get(i);
			int ptScore = pt.getFinalScore();
			if (ptScore > previousScore)
			{
				finishPos++;
			}
			
			pt.setFinishingPosition(finishPos);
			pt.saveToDatabase();
			
			previousScore = pt.getFinalScore();
		}
		
		updateScorersWithFinishingPositions();
		
		gameEntity.setDtFinish(DateUtil.getSqlDateNow());
		gameEntity.saveToDatabase();
		
		parentWindow.startNextGameIfNecessary();
		
		allPlayersFinished();
	}

	@Override
	public DartsScorerGolf factoryScorer()
	{
		return new DartsScorerGolf();
	}

	@Override
	public boolean shouldAIStop()
	{
		return false;
	}
	
	@Override
	protected void doMissAnimation()
	{
		dartboard.doGolfMiss();
	}
}
