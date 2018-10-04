package code.screen.reporting;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import object.SuperHashMap;
import screen.SimpleDialog;
import util.StringUtil;

public class ConfigureReportColumnsDialog extends SimpleDialog
{
	private static final String CONFIGURABLE_COLUMNS = "Type;Players;Start Date;Finish Date;Match";
	private SuperHashMap<String, JCheckBox> hmColumnNameToCheckBox = new SuperHashMap<>();
	
	public ConfigureReportColumnsDialog()
	{
		setSize(301, 251);
		setTitle("Configure Columns");
		setModal(true);
		
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[][][][]", "[][][]"));
		
		init();
	}
	
	private final JPanel panel = new JPanel();
	
	private void init()
	{
		ArrayList<String> columnNames = StringUtil.getListFromDelims(CONFIGURABLE_COLUMNS, ";");
		for (int i=0; i<columnNames.size(); i++)
		{
			String columnName = columnNames.get(i);
			JCheckBox cb = new JCheckBox(columnName);
			cb.setSelected(true);
			hmColumnNameToCheckBox.put(columnName, cb);
			
			panel.add(cb, "cell 1 " + (i+1));
		}
	}
	
	public boolean includeColumn(String columnName)
	{
		JCheckBox cb = hmColumnNameToCheckBox.get(columnName);
		if (cb == null)
		{
			//We don't have a checkbox for this column (so it's mandatory).
			return true;
		}
		
		return cb.isSelected();
	}
	
	@Override
	public void okPressed()
	{
		dispose();
	}
	
	@Override
	public boolean allowCancel()
	{
		return false;
	}
}
