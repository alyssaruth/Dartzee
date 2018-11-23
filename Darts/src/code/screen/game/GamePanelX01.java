package code.screen.game;

import code.ai.AbstractDartsModel;
import code.db.AchievementEntity;
import code.db.DartEntity;
import code.db.ParticipantEntity;
import code.object.Dart;
import code.utils.X01Util;
import object.HandyArrayList;
import object.HashMapList;
import util.Debug;

public class GamePanelX01 extends GamePanelPausable<DartsScorerX01>
{
	//Transient variables for each round
	private int startingScore = -1;
	private int currentScore = -1;
	
	public GamePanelX01(DartsGameScreen parent) 
	{
		super(parent);
	}
	
	@Override
	public void initImpl(String gameParams)
	{
		//Do nothing
	}
	
	@Override
	public void updateVariablesForNewRound()
	{
		startingScore = activeScorer.getLatestScoreRemaining();
		currentScore = startingScore;
	}
	
	@Override
	public void resetRoundVariables()
	{
		currentScore = startingScore;
	}
	
	@Override
	public void saveDartsAndProceed()
	{
		//Finalise the scorer
		Dart lastDart = dartsThrown.lastElement();
		boolean bust = X01Util.isBust(currentScore, lastDart);
		
		//Play a sound if we can...
		int totalScore = 0;
		for (Dart dart : dartsThrown)
		{
			totalScore += dart.getTotal();
		}
		
		if (!bust)
		{
			dartboard.playDodgySound("" + totalScore);
			
			int total = X01Util.sumScore(dartsThrown);
			AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE, getCurrentPlayerId(), getGameId(), total);
		}
		
		activeScorer.finaliseRoundScore(startingScore, bust);
		
		super.saveDartsAndProceed();
	}
	
	/**
	 * Loop through the darts thrown, saving them to the database.
	 */
	@Override
	public void saveDartsToDatabase(long roundId)
	{
		int score = startingScore;
		for (int i=0; i<dartsThrown.size(); i++)
		{
			Dart dart = dartsThrown.get(i);
			DartEntity.factoryAndSave(dart, roundId, i+1, score);
			
			//Adjust the starting score for the next DartEntity
			int total = dart.getTotal();
			score = score - total;
		}	
	}
	
	@Override
	public boolean currentPlayerHasFinished()
	{
		Dart lastDart = dartsThrown.lastElement();
		return currentScore == 0
		  && lastDart.isDouble();
	}
	
	@Override
	protected void updateAchievementsForFinish()
	{
		super.updateAchievementsForFinish();
		
		int sum = X01Util.sumScore(dartsThrown);
		AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_BEST_FINISH, getCurrentPlayerId(), getGameId(), sum);
		
		int checkout = dartsThrown.lastElement().getScore();
		AchievementEntity.insertIfNotExists(ACHIEVEMENT_REF_X01_ALL_FINISHES, getCurrentPlayerId(), getGameId(), checkout);
	}
		
	@Override
	public void loadDartsForParticipant(int playerNumber, HashMapList<Integer, Dart> hmRoundToDarts, int lastRound)
	{
		DartsScorerX01 scorer = hmPlayerNumberToDartsScorer.get(playerNumber);
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
	private void addDartsToScorer(HandyArrayList<Dart> darts, DartsScorerX01 scorer)
	{
		int startingScore = scorer.getLatestScoreRemaining();
		
		int score = startingScore;
		for (Dart dart : darts)
		{
			scorer.addDart(dart);
			
			score = score - dart.getTotal();
		}
		
		Dart lastDart = darts.lastElement();
		boolean bust = X01Util.isBust(score, lastDart);
		scorer.finaliseRoundScore(startingScore, bust);
	}
	
	@Override
	public void updateVariablesForDartThrown(Dart dart)
	{
		int dartTotal = dart.getTotal();
		currentScore = currentScore - dartTotal;
	}
	
	@Override
	public boolean shouldStopAfterDartThrown()
	{
		if (dartsThrown.size() == 3)
		{
			return true;
		}
		
		return currentScore <= 1;
	}
	
	@Override
	public void doAiTurn(AbstractDartsModel model)
	{
		if (X01Util.shouldStopForMercyRule(model, startingScore, currentScore))
		{
			Debug.append("MERCY RULE", VERBOSE_LOGGING);
			stopThrowing();
		}
		else
		{
			model.throwX01Dart(currentScore, dartboard);
		}
		
	}

	@Override
	public DartsScorerX01 factoryScorer()
	{
		return DartsScorerX01.factory(this);
	}
	
	@Override
	public GameStatisticsPanel factoryStatsPanel()
	{
		return new GameStatisticsPanelX01();
	}
}
