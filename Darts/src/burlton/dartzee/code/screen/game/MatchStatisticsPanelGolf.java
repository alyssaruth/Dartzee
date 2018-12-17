package burlton.dartzee.code.screen.game;

import java.util.ArrayList;

public class MatchStatisticsPanelGolf extends GameStatisticsPanelGolf
{
	@Override
	protected void addRowsToTable()
	{
		super.addRowsToTable();
		
		addRow(new Object[getRowWidth()]);
		
		addRow(getBestGameRow(s -> s.min()));
		addRow(getAverageGameRow());
	}
	
	@Override
	protected ArrayList<Integer> getRankedRowsLowestWins()
	{
		ArrayList<Integer> rows = super.getRankedRowsLowestWins();
		rows.add(13);
		rows.add(14);
		return rows;
	}
}
