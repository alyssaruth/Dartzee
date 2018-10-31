package code.screen.game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import code.object.Dart;
import object.HandyArrayList;
import util.MathsUtil;

public class GameStatisticsPanelGolf extends GameStatisticsPanel
{

	@Override
	protected void addRowsToTable()
	{
		addRow(getScoreRow(is -> (double)is.min().getAsInt(), "Best Hole"));
		addRow(getScoreRow(is -> MathsUtil.round(is.average().getAsDouble(), 2), "Avg. Hole"));
		addRow(getScoreRow(is -> (double)is.max().getAsInt(), "Worst Hole"));
		addRow(getMissesRow());
		
		addRow(new Object[getRowWidth()]);
		
		addRow(getScoreCountRow(1));
		addRow(getScoreCountRow(2));
		addRow(getScoreCountRow(3));
		addRow(getScoreCountRow(4));
		addRow(getScoreCountRow(5));
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
			ArrayList<HandyArrayList<Dart>> rounds = hmPlayerToDarts.get(playerName);
			
			IntStream is = rounds.stream().map(r -> r.lastElement()).mapToInt(d -> d.getGolfScore());
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
		return new ArrayList<>();
	}
	
	@Override
	protected ArrayList<Integer> getRankedRowsLowestWins()
	{
		return HandyArrayList.factoryAdd(0, 1, 2, 3);
	}
	
	@Override
	protected ArrayList<Integer> getHistogramRows()
	{
		return HandyArrayList.factoryAdd(5, 6, 7, 8, 9);
	}
}
