package burlton.dartzee.code.screen.game;

import java.util.stream.IntStream;

import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.utils.X01Util;
import burlton.core.code.obj.HandyArrayList;

public class MatchStatisticsPanelX01 extends GameStatisticsPanelX01
{
	@Override
	protected void addRowsToTable()
	{
		super.addRowsToTable();
		
		addRow(getHighestFinishRow());
		
		addRow(new Object[getRowWidth()]);
		
		addRow(getBestGameRow(s -> s.min()));
		addRow(getAverageGameRow());
	}
	
	private Object[] getHighestFinishRow()
	{
		Object[] row = new Object[getRowWidth()];
		row[0] = "Best Finish";
		
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			HandyArrayList<HandyArrayList<Dart>> rounds = hmPlayerToDarts.get(playerName);
			
			HandyArrayList<HandyArrayList<Dart>> finishRounds = rounds.createFilteredCopy(r -> X01Util.isFinishRound(r));
			if (finishRounds.isEmpty())
			{
				row[i+1] = "N/A";
			}
			else
			{
				IntStream is = finishRounds.stream().mapToInt(r -> X01Util.sumScore(r));
				int max = is.max().getAsInt();
				
				row[i+1] = max;
			}
		}
		
		return row;
	}
}
