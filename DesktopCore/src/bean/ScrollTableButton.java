package bean;

import javax.swing.Action;
import javax.swing.table.DefaultTableModel;

public class ScrollTableButton extends ScrollTable
{
	private int buttonColumn = -1;
	
	public ScrollTableButton(int buttonColumn, DefaultTableModel tm, Action a)
	{
		this.buttonColumn = buttonColumn;
		
		setModel(tm);
		ButtonColumn bc = new ButtonColumn(this, a, buttonColumn);
	}
	
	@Override
	public boolean isEditable(int row, int col)
	{
		return col == buttonColumn;
	}
}
