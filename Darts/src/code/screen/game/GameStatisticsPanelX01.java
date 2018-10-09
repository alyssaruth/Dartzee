package code.screen.game;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import code.db.ParticipantEntity;
import code.object.Dart;
import code.utils.X01Util;

/**
 * Shows running stats for X01 games - three-dart average, checkout % etc.
 */
public final class GameStatisticsPanelX01 extends GameStatisticsPanel
{
	@Override
	protected void buildTableModel()
	{
		DefaultTableModel tm = new DefaultTableModel();
		tm.addColumn("");
		
		ArrayList<String> playerNamesOrdered = new ArrayList<>();
		for (int i=0; i<4; i++)
		{
			ParticipantEntity pt = hmPlayerNumberToParticipant.get(i);
			if (pt != null)
			{
				String playerName = pt.getPlayerName();
				playerNamesOrdered.add(playerName);
				tm.addColumn(playerName);
			}
		}
		
		int rowSize = playerNamesOrdered.size() + 1;
		
		//3-dart average
		Object[] threeDartAvgs = new Object[rowSize];
		threeDartAvgs[0] = "3-dart avg";
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			ArrayList<Dart> darts = hmPlayerToDarts.get(playerName);
			
			double avg = X01Util.calculateThreeDartAverage(darts, 140);
			threeDartAvgs[i+1] = avg;
		}
		
		tm.addRow(threeDartAvgs);
		
		table.setModel(tm);
		
		
	}

}
