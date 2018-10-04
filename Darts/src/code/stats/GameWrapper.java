package code.stats;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import code.object.Dart;
import code.screen.game.DartsScorerGolf;
import code.screen.stats.player.HoleBreakdownWrapper;
import object.HandyArrayList;
import object.HashMapList;
import util.DateUtil;
import util.Debug;

/**
 * Wraps up an entire game of darts from a single player's perspective
 */
public class GameWrapper
{
	public static final int MODE_FRONT_9 = 0;
	public static final int MODE_BACK_9 = 1;
	public static final int MODE_FULL_18 = 2;
	
	private HashMapList<Integer, Dart> hmRoundNumberToDarts = new HashMapList<>();
	
	private long gameId = -1;
	private String gameParams = "";
	private Timestamp dtStart = null;
	private Timestamp dtFinish = DateUtil.END_OF_TIME;
	private int finalScore = -1;
	
	private int totalRounds = 0;
	
	public GameWrapper(long gameId, String gameParams, Timestamp dtStart, Timestamp dtFinish, int finalScore)
	{
		this.gameId = gameId;
		this.gameParams = gameParams;
		this.dtStart = dtStart;
		this.dtFinish = dtFinish;
		this.finalScore = finalScore;
	}
	
	/**
	 * Helpers
	 */
	public HandyArrayList<Dart> getAllDarts()
	{
		return hmRoundNumberToDarts.getAllValues();
	}
	public boolean isFinished()
	{
		return finalScore > -1;
	}
	public void addDart(int roundNumber, Dart dart)
	{
		if (roundNumber > totalRounds)
		{
			totalRounds = roundNumber;
		}
		
		hmRoundNumberToDarts.putInList(roundNumber, dart);
	}
	public int getScoreForFinalRound()
	{
		return getScoreForRound(totalRounds);
	}
	public HandyArrayList<Dart> getDartsForFinalRound()
	{
		return getDartsForRound(totalRounds);
	}
	private HandyArrayList<Dart> getDartsForRound(int roundNumber)
	{
		return hmRoundNumberToDarts.get(roundNumber);
	}
	private int getScoreForRound(int roundNumber)
	{
		ArrayList<Dart> darts = getDartsForRound(roundNumber);
		return getTotalForDarts(darts);
	}
	private int getTotalForDarts(ArrayList<Dart> darts)
	{
		int total = 0;
		for (int j=0; j<darts.size(); j++)
		{
			Dart dart = darts.get(j);
			total += dart.getTotal();
		}
		
		return total;
	}
	
	/**
	 * X01 Helpers
	 */
	public int getCheckoutTotal()
	{
		//For unfinished games, return -1 so they're sorted to the back
		if (finalScore == -1)
		{
			return -1;
		}
		
		return getScoreForFinalRound();
	}
	
	/**
	 * Calculate the 3-dart average, only counting the darts that were thrown up to a certain point. 
	 *  N.B: This method does NOT handle 'busts' when considering whether you've gone below the score threshold. 
	 *  Therefore, the smallest threshold that should ever be passed in is 62. 
	 */
	public double getThreeDartAverage(int scoreCutOff)
	{
		ArrayList<Dart> darts = getScoringDarts(scoreCutOff);
		if (darts == null)
		{
			return -1;
		}
		
		double totalScoringDarts = darts.size();
		double amountScored = 0;
		for (Dart dart : darts)
		{
			amountScored += dart.getTotal();
		}
		
		return (amountScored / totalScoringDarts) * 3;
	}
	
	public int getMissedDartsX01(int scoreCutOff)
	{
		ArrayList<Dart> darts = getScoringDarts(scoreCutOff);
		if (darts == null)
		{
			return -1;
		}
		
		int misses = 0;
		for (Dart dart : darts)
		{
			if (dart.getTotal() == 0)
			{
				misses++;
			}
		}
		
		return misses;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Dart> getScoringDarts(int scoreCutOff)
	{
		return (ArrayList<Dart>)getScoringDartVectors(scoreCutOff).get(0);
	}
	@SuppressWarnings("unchecked")
	public ArrayList<ArrayList<Dart>> getScoringDartsGroupedByRound(int scoreCutOff)
	{
		return (ArrayList<ArrayList<Dart>>)getScoringDartVectors(scoreCutOff).get(1);
	}
	private ArrayList<ArrayList<?>> getScoringDartVectors(int scoreCutOff)
	{
		if (scoreCutOff < 62)
		{
			Debug.stackTrace("Calculating scoring darts with cutoff that makes busts possible: " + scoreCutOff);
			return null;
		}
		
		ArrayList<ArrayList<Dart>> allDartsInRounds = new ArrayList<>();
		ArrayList<Dart> allDarts = new ArrayList<>();
		
		HandyArrayList<ArrayList<?>> ret = HandyArrayList.factoryAdd(allDarts, allDartsInRounds);
		
		int score = Integer.parseInt(gameParams);
		for (int i=1; i<=totalRounds; i++)
		{
			ArrayList<Dart> drts = getDartsForRound(i);
			for (int j=0; j<drts.size(); j++)
			{
				Dart dart = drts.get(j);
				allDarts.add(dart);
				
				if (j == drts.size() - 1)
				{
					//We've got a full round, add this before we return
					allDartsInRounds.add(drts);
				}
				
				score -= dart.getTotal();
				if (score <= scoreCutOff)
				{
					return ret;
				}
			}
		}
		
		if (isFinished())
		{
			Debug.stackTrace("Unable to calculate scoring darts for finished game " + gameId + ". Score never went below threshold: " + scoreCutOff);
			return null;
		}
		
		//If we get to here, it's an unfinished game that never went below the threshold. THat's fine - just return what we've got.
		return ret;
	}
	
	/**
	 * Burlton Constant
	 */
	public void populateThreeDartScoreMap(HashMap<Integer, ThreeDartScoreWrapper> hmScoreToBreakdownWrapper, int scoreThreshold)
	{
		ArrayList<ArrayList<Dart>> dartsRounds = getScoringDartsGroupedByRound(scoreThreshold);
		
		for (int i=0; i<dartsRounds.size(); i++)
		{
			ArrayList<Dart> dartsForRound = dartsRounds.get(i);
			int score = getTotalForDarts(dartsForRound);
			
			ThreeDartScoreWrapper wrapper = hmScoreToBreakdownWrapper.get(score);
			if (wrapper == null)
			{
				wrapper = new ThreeDartScoreWrapper();
				hmScoreToBreakdownWrapper.put(score, wrapper);
			}
			
			String dartStr = Dart.getSortedDartStr(dartsForRound);
			wrapper.addDartStr(dartStr, gameId);
		}
	}
	
	/**
	 * Golf Helpers
	 */
	public int getScoreForHole(int hole)
	{
		HandyArrayList<Dart> darts = getDartsForRound(hole);
		Dart scoringDart = darts.lastElement();
		return scoringDart.getGolfScore(hole);
	}
	public void updateHoleBreakdowns(HashMap<Integer, HoleBreakdownWrapper> hm)
	{
		HoleBreakdownWrapper overallBreakdown = hm.get(-1);
		if (overallBreakdown == null)
		{
			overallBreakdown = new HoleBreakdownWrapper();
			hm.put(-1, overallBreakdown);
		}
		
		for (int i=1; i<=totalRounds; i++)
		{
			HoleBreakdownWrapper wrapper = hm.get(i);
			if (wrapper == null)
			{
				wrapper = new HoleBreakdownWrapper();
				hm.put(i,  wrapper);
			}
			
			int score = getScoreForHole(i);
			wrapper.increment(score);
			
			//Increment an overall one
			overallBreakdown.increment(score);
		}
	}
	
	/**
	 * Get the overall score for front 9, back 9 or the whole lot
	 */
	public Integer getRoundScore(int mode)
	{
		int startHole = getStartHoleForMode(mode);
		int endHole = getEndHoleForMode(mode);

		return getScore(startHole, endHole);
	}
	private int getScore(int startHole, int finishHole)
	{
		if (totalRounds < finishHole)
		{
			//We haven't completed all the necessary rounds
			return -1;
		}
		
		int total = 0;
		for (int i=startHole; i<=finishHole; i++)
		{
			total += getScoreForHole(i);
		}
		
		return total;
	}
	
	public void populateScorer(DartsScorerGolf scorer, int mode)
	{
		int startHole = getStartHoleForMode(mode);
		int endHole = getEndHoleForMode(mode);
		for (int i=startHole; i<=endHole; i++)
		{
			ArrayList<Dart> darts = getDartsForRound(i);
			scorer.addDarts(darts);
		}
	}
	
	private int getStartHoleForMode(int mode)
	{
		if (mode == MODE_BACK_9)
		{
			return 10;
		}
		
		return 1;
	}
	private int getEndHoleForMode(int mode)
	{
		if (mode == MODE_FRONT_9)
		{
			return 9;
		}
		
		return 18;
	}
	public void populateOptimalScorecardMaps(HashMapList<Integer, Dart> hmHoleToBestDarts, HashMap<Integer, Long> hmHoleToBestGameId)
	{
		for (int i=1; i<=totalRounds; i++)
		{
			HandyArrayList<Dart> darts = getDartsForRound(i);
			HandyArrayList<Dart> currentDarts = hmHoleToBestDarts.get(i);
			
			if (isBetterGolfRound(i, darts, currentDarts))
			{
				hmHoleToBestDarts.put(i, darts);
				hmHoleToBestGameId.put(i, gameId);
			}
		}
	}
	private boolean isBetterGolfRound(int hole, HandyArrayList<Dart> dartsNew, HandyArrayList<Dart> dartsCurrent)
	{
		if (dartsCurrent == null)
		{
			return true;
		}
		
		Dart lastDart = dartsNew.lastElement();
		int scoreNew = lastDart.getGolfScore(hole);
		
		lastDart = dartsCurrent.lastElement();
		int scoreCurrent = lastDart.getGolfScore(hole);
		
		//If the new score is strictly less, then it's better
		if (scoreNew < scoreCurrent)
		{
			return true;
		}

		if (scoreNew > scoreCurrent)
		{
			return false;
		}
		
		//Equal scores, so go on number of darts thrown. Less is better.
		int newSize = dartsNew.size();
		int currentSize = dartsCurrent.size();
		return newSize < currentSize;
	}
	
	/**
	 * Generic helpers
	 */
	public int compareStartDate(GameWrapper other)
	{
		return dtStart.compareTo(other.getDtStart());
	}
	
	
	/**
	 * Getters
	 */
	public long getGameId()
	{
		return gameId;
	}
	public int getGameStartValueX01()
	{
		return Integer.parseInt(gameParams);
	}
	public String getGameParams()
	{
		return gameParams;
	}
	public Timestamp getDtStart()
	{
		return dtStart;
	}
	public Timestamp getDtFinish()
	{
		return dtFinish;
	}
	public int getFinalScore()
	{
		return finalScore;
	}
	
	/**
	 * These are normally calculated when adding darts retrieved from the DB.
	 * Need direct setters if we're from a simulation
	 */
	public void setTotalRounds(int totalRounds)
	{
		this.totalRounds = totalRounds;
	}
	public void setHmRoundNumberToDartsThrown(HashMapList<Integer, Dart> hmRoundNumberToDarts)
	{
		this.hmRoundNumberToDarts = hmRoundNumberToDarts;
	}
}
