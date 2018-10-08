package code.screen.game;

import javax.swing.table.DefaultTableModel;

import object.HandyArrayList;

/**
 * Shows running stats for X01 games - three-dart average, checkout % etc.
 */
public final class GameStatisticsPanelX01 extends GameStatisticsPanel
{
	@Override
	protected void buildTableModel()
	{
		HandyArrayList<String> playerNames = hmPlayerToDarts.getKeysAsVector();
		
		DefaultTableModel tm = new DefaultTableModel();
		tm.addColumn("");
		
		for (String playerName : playerNames)
		{
			tm.addColumn(playerName);
		}
		
		table.setModel(tm);
	}

}
