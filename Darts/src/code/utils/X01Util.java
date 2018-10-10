package code.utils;

import java.util.ArrayList;

import code.ai.AbstractDartsModel;
import code.object.Dart;
import object.HandyArrayList;

public abstract class X01Util
{
	public static boolean isBust(int score, Dart lastDart)
	{
		return score < 0
		  || score == 1
		  || (score == 0 && !lastDart.isDouble());
	}
	
	/**
	 * Apply the Mercy Rule if:
	 *  - It has been enabled for this AI
	 *  - The starting score was odd and < the threshold (configurable per AI)
	 *  - The current score is even, meaing we have bailed ourselves out in some way
	 */
	public static boolean shouldStopForMercyRule(AbstractDartsModel model, int startingScore, int currentScore)
	{
		int mercyThreshold = model.getMercyThreshold();
		if (mercyThreshold == -1)
		{
			return false;
		}
		
		return startingScore < mercyThreshold
		  && startingScore % 2 != 0
		  && currentScore % 2 == 0;
	}
	
	/**
	 * 50, 40, 38, 36, 34, ... , 8, 4, 2
	 */
	public static boolean isCheckoutDart(Dart drt)
	{
		int startingScore = drt.getStartingScore();
		
		//Special case for bullseye
		if (startingScore == 50)
		{
			return true;
		}
		
		return startingScore % 2 == 0 //Even
		  && startingScore <= 40;
	}
	
	/**
	 * Refactored out of GameWrapper for use in game stats panel
	 */
	public static HandyArrayList<Dart> getScoringDarts(HandyArrayList<Dart> allDarts, int scoreCutOff)
	{
		if (allDarts == null)
		{
			return new HandyArrayList<>();
		}
		
		return allDarts.createFilteredCopy(d -> d.getStartingScore() > scoreCutOff);
	}
	public static double calculateThreeDartAverage(HandyArrayList<Dart> darts, int scoreCutOff)
	{
		ArrayList<Dart> scoringDarts = getScoringDarts(darts, scoreCutOff);
		
		double totalScoringDarts = scoringDarts.size();
		double amountScored = 0;
		for (Dart dart : scoringDarts)
		{
			amountScored += dart.getTotal();
		}
		
		return (amountScored / totalScoringDarts) * 3;
	}
	
	public static int sumScore(HandyArrayList<Dart> darts)
	{
		return darts.stream().mapToInt(d -> d.getTotal()).sum();
	}
}
