package code.db.sanity;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import bean.ScrollTable;
import code.utils.DatabaseUtil;
import object.HandyArrayList;
import util.DialogUtil;
import util.StringUtil;
import util.TableUtil.DefaultModel;

public final class SanityCheckResultUnexpectedTables extends SanityCheckResultSimpleTableModel
{
	public SanityCheckResultUnexpectedTables(DefaultModel model)
	{
		super(model, "Unexpected Tables");
	}
	
	@Override
	public Action getDeleteAction(ScrollTable t)
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
	private boolean deleteSelectedTables(HandyArrayList<String> tableNames)
	{
		boolean success = true;
		for (String tableName : tableNames)
		{
			success = DatabaseUtil.dropTable(tableName) && success;
		}
		
		return success;
	}
}
