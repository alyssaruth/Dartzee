package burlton.desktopcore.code.bean;

import javax.swing.Action;
import javax.swing.table.DefaultTableModel;

import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.obj.SuperHashMap;

public class ScrollTableButton extends ScrollTable
{
	private HandyArrayList<Integer> buttonColumns = new HandyArrayList<>();
	
	public ScrollTableButton(int buttonColumn, DefaultTableModel tm, Action a)
	{
		this.buttonColumns = HandyArrayList.factoryAdd(buttonColumn);
		
		setModel(tm);
		
		ButtonColumn bc = new ButtonColumn(this, a, buttonColumn);
	}
	
	public ScrollTableButton(SuperHashMap<Integer, Action> hmColumnToAction, DefaultTableModel tm)
	{
		this.buttonColumns = hmColumnToAction.getKeysAsVector();
		
		setModel(tm);
		
		for (int buttonColumn : buttonColumns)
		{
			ButtonColumn bc = new ButtonColumn(this, hmColumnToAction.get(buttonColumn), buttonColumn);
		}
	}
	
	@Override
	public boolean isEditable(int row, int col)
	{
		return buttonColumns.contains(col);
	}
}
