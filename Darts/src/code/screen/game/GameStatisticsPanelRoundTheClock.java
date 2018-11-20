package code.screen.game;

import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.IntStream;

import code.object.Dart;
import object.HandyArrayList;
import util.MathsUtil;

public class GameStatisticsPanelRoundTheClock extends GameStatisticsPanel
{

	@Override
	protected void addRowsToTable()
	{
		addRow(getDartsPerNumber(i -> getMaxDartsForAnyRound(i), "Most darts", true));
		addRow(getDartsPerNumber(i -> getAverageDartsForAnyRound(i), "Avg. darts", false));
		addRow(getDartsPerNumber(i -> getMinDartsForAnyRound(i), "Fewest darts", false));
		
		addRow(new Object[getRowWidth()]);
		
		addRow(getLongestStreak());
		addRow(getBruceys("Brucey chances", false));
		addRow(getBruceys("Bruceys executed", true));
		
		addRow(new Object[getRowWidth()]);
		
		addRow(getDartsPerNumber(1, 1, "1")); 
		addRow(getDartsPerNumber(2, 3));
		addRow(getDartsPerNumber(4, 6));
		addRow(getDartsPerNumber(6, 10));
		addRow(getDartsPerNumber(10, 15));
		addRow(getDartsPerNumber(16, 20));
		addRow(getDartsPerNumber(21, Integer.MAX_VALUE, "21+"));
		
		table.setColumnWidths("140");
	}
	
	private Object getMaxDartsForAnyRound(IntStream darts)
	{
		//This includes incomplete rounds, so there'll always be something.#
		return darts.max().getAsInt();
	}
	private Object getAverageDartsForAnyRound(IntStream darts)
	{
		OptionalDouble oi = darts.average();
		if (!oi.isPresent())
		{
			return "N/A";
		}
		
		return MathsUtil.round(oi.getAsDouble(), 2);
	}
	private Object getMinDartsForAnyRound(IntStream darts)
	{
		OptionalInt oi = darts.min();
		if (!oi.isPresent())
		{
			return "N/A";
		}
		
		return oi.getAsInt();
	}
	
	private Object[] getBruceys(String desc, boolean enforceSuccess)
	{
		Object[] row = factoryRow(desc);
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			
			HandyArrayList<HandyArrayList<Dart>> rounds = hmPlayerToDarts.get(playerName);
			HandyArrayList<HandyArrayList<Dart>> bruceyRounds = rounds.createFilteredCopy(r -> r.size() == 4);
			
			if (enforceSuccess)
			{
				bruceyRounds = bruceyRounds.createFilteredCopy(r -> r.lastElement().hitClockTarget(gameParams));
			}
			
			row[i+1] = bruceyRounds.size();
		}
		
		return row;
	}
	
	private Object[] getLongestStreak()
	{
		Object[] row = factoryRow("Best Streak");
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			
			HandyArrayList<Dart> darts = getFlattenedDarts(playerName);
			row[i+1] = getLongestStreak(darts);
		}
		
		return row;
	}
	
	private int getLongestStreak(HandyArrayList<Dart> darts)
	{
		int biggestChain = 0;
		int currentChain = 0;
		long currentPtId = -1;
		
		for (Dart d : darts)
		{
			if (!d.hitClockTarget(gameParams))
			{
				currentChain = 0;
				continue;
			}
			
			if (d.getParticipantId() != currentPtId)
			{
				currentChain = 0;
				currentPtId = d.getParticipantId();
			}
			
			//It's a hit and we've reset for a new game if necessary. Just increment.
			currentChain++;
			if (currentChain > biggestChain)
			{
				biggestChain = currentChain;
			}
		}
		
		return biggestChain;
	}
	
	private Object[] getDartsPerNumber(int min, int max)
	{
		return getDartsPerNumber(min, max, null);
	}
	private Object[] getDartsPerNumber(int min, int max, String desc)
	{
		if (desc == null)
		{
			desc = min + " - " + max;
		}
		
		Object[] row = factoryRow(desc);
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			
			HandyArrayList<HandyArrayList<Dart>> dartsGrouped = getDartsGroupedByParticipantAndNumber(playerName);
			row[i+1] = dartsGrouped.stream()
								   .mapToInt(g -> g.size())
								   .filter(ix -> (ix >= min && ix <= max))
								   .count();
		}
		
		return row;
	}
	
	private Object[] getDartsPerNumber(Function<IntStream, Object> fn, String desc, boolean includeUnfinished)
	{
		Object[] row = factoryRow(desc);
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			
			HandyArrayList<HandyArrayList<Dart>> dartsGrouped = getDartsGroupedByParticipantAndNumber(playerName);
			if (!includeUnfinished)
			{
				dartsGrouped = dartsGrouped.createFilteredCopy(g -> g.lastElement().hitClockTarget(gameParams));
			}
			
			IntStream is = dartsGrouped.stream().mapToInt(g -> g.size());
			row[i+1] = fn.apply(is);
		}
		
		return row;
	}
	private HandyArrayList<HandyArrayList<Dart>> getDartsGroupedByParticipantAndNumber(String playerName)
	{
		HandyArrayList<Dart> darts = getFlattenedDarts(playerName);
		Function<Dart, String> fnIdentifier = (d -> d.getParticipantId() + "_" + d.getStartingScore());
		return darts.groupBy(fnIdentifier);
	}

	@Override
	protected ArrayList<Integer> getRankedRowsHighestWins()
	{
		return HandyArrayList.factoryAdd(4, 5, 6);
	}

	@Override
	protected ArrayList<Integer> getRankedRowsLowestWins()
	{
		return HandyArrayList.factoryAdd(0, 1, 2);
	}

	@Override
	protected ArrayList<Integer> getHistogramRows()
	{
		return HandyArrayList.factoryAdd(8, 9, 10, 11, 12, 13, 14);
	}

}
