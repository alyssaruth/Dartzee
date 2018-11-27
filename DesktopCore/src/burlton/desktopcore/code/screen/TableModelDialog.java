package burlton.desktopcore.code.screen;

import java.awt.BorderLayout;

import javax.swing.table.DefaultTableModel;

import burlton.desktopcore.code.bean.ScrollTable;

/**
 * Simple dialog to show a table
 */
public class TableModelDialog extends SimpleDialog
{
	public TableModelDialog(String title, DefaultTableModel model)
	{
		table.setModel(model);
		
		setTitle(title);
		setSize(600, 400);
		setModal(true);
		
		getContentPane().add(table, BorderLayout.CENTER);
	}
	public TableModelDialog(String title, ScrollTable table) 
	{	
		this.table = table;
		
		setTitle(title);
		setSize(600, 400);
		setModal(true);
		
		getContentPane().add(table, BorderLayout.CENTER);
	}
	
	private ScrollTable table = new ScrollTable();
	
	/**
	 * Configure things about the table
	 */
	public void setColumnWidths(String colWidthsStr)
	{
		table.setColumnWidths(colWidthsStr);
	}
	public ScrollTable getTable()
	{
		return table;
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