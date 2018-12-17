package burlton.dartzee.code.screen.game;

import java.util.ArrayList;

public class MatchStatisticsPanelRoundTheClock extends GameStatisticsPanelRoundTheClock
{
	@Override
	protected void addRowsToTable()
	{
		super.addRowsToTable();
		
		addRow(new Object[getRowWidth()]);
		addRow(getBestGameRow(is -> is.min()));
		addRow(getAverageGameRow());
	}
	
	@Override
	protected ArrayList<Integer> getRankedRowsLowestWins()
	{
		ArrayList<Integer> ret = super.getRankedRowsLowestWins();
		ret.add(16);
		ret.add(17);
		return ret;
	}
}
