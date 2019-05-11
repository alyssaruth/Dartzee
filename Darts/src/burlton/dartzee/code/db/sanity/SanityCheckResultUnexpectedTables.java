package burlton.dartzee.code.db.sanity;

import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.util.StringUtil;
import burlton.dartzee.code.utils.DatabaseUtil;
import burlton.desktopcore.code.bean.ScrollTable;
import burlton.desktopcore.code.util.DialogUtil;
import burlton.desktopcore.code.util.TableUtil.DefaultModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;

public final class SanityCheckResultUnexpectedTables extends SanityCheckResultSimpleTableModel
{
	public SanityCheckResultUnexpectedTables(DefaultModel model)
	{
		super(model, "Unexpected Tables");
	}
	
	@Override
	public AbstractAction getDeleteAction(ScrollTable t)
	{
		return new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				int[] rows = t.getSelectedModelRows();
				if (rows.length == 0)
				{
					return;
				}
				
				HandyArrayList<String> tableNames = new HandyArrayList<>();
				for (int i=0; i<rows.length; i++)
				{
					String tableName = (String)t.getValueAt(rows[i], 1);
					tableNames.add(tableName);
				}
				
				String tableList = StringUtil.toDelims(tableNames, "\n");
				int ans = DialogUtil.showQuestion("Are you sure you want to drop the following tables from the database?\n\n" + tableList, false);
				if (ans == JOptionPane.YES_OPTION)
				{
					boolean success = deleteSelectedTables(tableNames);
					if (!success)
					{
						DialogUtil.showError("An error occurred dropping the tables. You should re-run the sanity check and check logs.");
					}
				}
			}
		};
	}
	
	private HandyArrayList<String> getTableNames()
	{
		DefaultTableModel model = getResultsModel();
		
		int rowCount = model.getRowCount();
		
		HandyArrayList<String> tableNames = new HandyArrayList<>();
		for (int i=0; i<rowCount; i++)
		{
			String tableName = (String)model.getValueAt(i, 1);
			tableNames.add(tableName);
		}
		
		return tableNames;
	}
	
	private boolean deleteSelectedTables(HandyArrayList<String> tableNames)
	{
		boolean success = true;
		for (String tableName : tableNames)
		{
			success = DatabaseUtil.dropTable(tableName) && success;
		}
		
		return success;
	}
	
	@Override
	public void autoFix()
	{
		HandyArrayList<String> tableNames = getTableNames();
		
		int response = DialogUtil.showQuestion("This will drop all " + tableNames.size() + " tables from the database. Are you sure?", false);
		if (response == JOptionPane.NO_OPTION)
		{
			return;
		}
		
		boolean success = deleteSelectedTables(tableNames);
		if (!success)
		{
			DialogUtil.showError("An error occurred dropping the tables. You should re-run the sanity check and check logs.");
		}
	}
}
