package burlton.dartzee.code.db.sanity;

import burlton.core.code.obj.HandyArrayList;
import burlton.dartzee.code.db.AbstractEntity;
import burlton.dartzee.code.db.GameEntity;
import burlton.desktopcore.code.util.DialogUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Check for Games as part of the same match but with the same ordinal. 
 * Originally happened due to a bug where the ordinal reset upon re-loading an incomplete match
 */
public final class SanityCheckResultDuplicateMatchOrdinals extends SanityCheckResultEntitiesSimple
{
	public SanityCheckResultDuplicateMatchOrdinals(List<? extends AbstractEntity<?>> entities, String description)
	{
		super(entities, description);
	}

	@Override
	public void autoFix()
	{
		DefaultTableModel tm = getResultsModel();
		int rowCount = tm.getRowCount();
		
		//Get the distinct matches affected
		HandyArrayList<Long> matchIds = new HandyArrayList<>();
		for (int i=0; i<rowCount; i++)
		{
			long matchId = (long)tm.getValueAt(i, 6);
			matchIds.addUnique(matchId);
		}
		
		//Just double-check...
		int ans = DialogUtil.showQuestion("This will reset the ordinal for all games in " + matchIds.size() + " matches. Proceed?", false);
		if (ans == JOptionPane.NO_OPTION)
		{
			return;
		}
		
		//Fix the matches one at a time
		for (long matchId : matchIds)
		{
			String gameSql = "DartsMatchId = " + matchId + " ORDER BY RowId";
			List<GameEntity> games = new GameEntity().retrieveEntities(gameSql);
			
			int ordinal = 0;
			for (GameEntity game : games)
			{
				game.setMatchOrdinal(ordinal);
				game.saveToDatabase();
				
				ordinal++;
			}
		}
		
		DialogUtil.showInfo("Auto-fix complete. You should re-run the sanity check and check there are no errors.");
	}
}
