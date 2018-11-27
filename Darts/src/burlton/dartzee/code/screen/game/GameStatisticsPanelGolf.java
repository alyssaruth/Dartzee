package burlton.dartzee.code.screen.game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import burlton.dartzee.code.object.Dart;
import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.util.MathsUtil;

public class GameStatisticsPanelGolf extends GameStatisticsPanel
{

	@Override
	protected void addRowsToTable()
	{
		addRow(getScoreRow(is -> (double)is.min().getAsInt(), "Best Hole"));
		addRow(getScoreRow(is -> MathsUtil.round(is.average().getAsDouble(), 2), "Avg. Hole"));
		addRow(getScoreRow(is -> (double)is.max().getAsInt(), "Worst Hole"));
		addRow(getMissesRow());
		addRow(getGambleRow(r -> getPointsSquandered(r), "Points Squandered"));
		addRow(getGambleRow(r -> getPointsImproved(r), "Points Improved"));
		
		addRow(new Object[getRowWidth()]);
		
		addRow(getScoreCountRow(1));
		addRow(getScoreCountRow(2));
		addRow(getScoreCountRow(3));
		addRow(getScoreCountRow(4));
		addRow(getScoreCountRow(5));
		
		table.setColumnWidths("150");
	}
	
	/**
	 * Any round where you could have "banked" and ended on something higher.
	 */
	private Object[] getGambleRow(Function<HandyArrayList<Dart>, Integer> f, String desc)
	{
		Object[] pointsSquandered = new Object[getRowWidth()];
		pointsSquandered[0] = desc;
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			ArrayList<HandyArrayList<Dart>> rounds = hmPlayerToDarts.get(playerName);
			
			pointsSquandered[i+1] = rounds.stream().mapToInt(r -> f.apply(r)).sum();
		}
		
		return pointsSquandered;
	}
	
	private int getPointsSquandered(HandyArrayList<Dart> round)
	{
		int finalScore = round.lastElement().getGolfScore();
		int bestScore = round.stream().mapToInt(d -> d.getGolfScore()).min().getAsInt();
		
		return finalScore - bestScore;
	}
	
	/**
	 * A bit difficult to define. Some examples:
	 * 
	 * 4-3-2. You've gambled twice, and gained 1 each time. So method should return 2.
	 * 3-4-2. You've gambled the 3, stuffed it, then clawed it back. Method should return 1.
	 * 5-5-1. You've not gambled anything. Method should return 0.
	 * 4-3-5. You've stuffed it - there was a gain but it's gone. Method should return 0.
	 * 4-2-3. You've gained 1 (and also lost 1). Method should return 1 for the original '4' gamble. I guess.
	 */
	private int getPointsImproved(HandyArrayList<Dart> round)
	{
		int finalScore = round.lastElement().getGolfScore();
		int bestScore = round.stream().mapToInt(d -> d.getGolfScore()).min().getAsInt();
		
		//This round is stuffed - points have been squandered, not gained! Or it's just 1 dart!
		if (finalScore > bestScore
		  || round.size() == 1)
		{
			return 0;
		}
		
		//Filter out the 5s - they're not interesting.
		HandyArrayList<Dart> roundWithoutMisses = round.createFilteredCopy(d -> d.getGolfScore() < 5);
		if (roundWithoutMisses.isEmpty())
		{
			//Round is all misses, so nothing to do
			return 0;
		}
		
		//Now get the first non-5. Result is the difference between this and where you ended up.
		int gambledScore = roundWithoutMisses.firstElement().getGolfScore();
		return gambledScore - bestScore;
	}
	
	
	private Object[] getScoreCountRow(int score)
	{
		Object[] row = new Object[getRowWidth()];
		row[0] = "" + score;
		
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			HandyArrayList<Dart> darts = getCountedDarts(playerName);
			HandyArrayList<Dart> dartsOfScore = darts.createFilteredCopy(d -> d.getGolfScore() == score);
			
			row[i+1] = dartsOfScore.size();
		}
		
		return row;
	}
	private Object[] getScoreRow(Function<IntStream, Double> f, String desc)
	{
		Object[] row = new Object[getRowWidth()];
		row[0] = desc;
		
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			HandyArrayList<Dart> countedDarts = getCountedDarts(playerName);
			IntStream is = countedDarts.stream().mapToInt(d -> d.getGolfScore());
			row[i+1] = f.apply(is);
		}
		
		return row;
	}
	
	/**
	 * Get the darts that were actually counted, i.e. the last of each round.
	 */
	private HandyArrayList<Dart> getCountedDarts(String playerName)
	{
		ArrayList<HandyArrayList<Dart>> rounds = hmPlayerToDarts.get(playerName);
		
		List<Dart> darts = rounds.stream().map(r -> r.lastElement()).collect(Collectors.toList());
		
		return new HandyArrayList<>(darts);
	}
	private Object[] getMissesRow()
	{
		Object[] row = new Object[getRowWidth()];
		row[0] = "Miss %";
		
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			HandyArrayList<Dart> darts = getFlattenedDarts(playerName);
			ArrayList<Dart> missDarts = darts.createFilteredCopy(d -> d.getGolfScore() == 5);
			
			double misses = missDarts.size();
			double percent = 100 * misses / darts.size();
			
			row[i+1] = MathsUtil.round(percent, 2);
		}
		
		return row;
	}
	
	@Override
	protected ArrayList<Integer> getRankedRowsHighestWins()
	{
		return HandyArrayList.factoryAdd(5);
	}
	
	@Override
	protected ArrayList<Integer> getRankedRowsLowestWins()
	{
		return HandyArrayList.factoryAdd(0, 1, 2, 3, 4);
	}
	
	@Override
	protected ArrayList<Integer> getHistogramRows()
	{
		return HandyArrayList.factoryAdd(7, 8, 9, 10, 11);
	}
}
