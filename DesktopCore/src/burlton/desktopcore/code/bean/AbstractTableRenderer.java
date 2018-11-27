package burlton.desktopcore.code.bean;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import burlton.core.code.util.Debug;

/**
 * Abstract extension of DefaultTableCellRenderer to allow parameterisation using generics. 
 * Also provides a place to do extra checks (e.g. null values).
 */
public abstract class AbstractTableRenderer<E> extends DefaultTableCellRenderer 
{
	public abstract Object getReplacementValue(E object);
	
	@Override
	@SuppressWarnings("unchecked")
    public Component getTableCellRendererComponent(JTable table, Object
        value, boolean isSelected, boolean hasFocus, int row, int column) 
    {
		try
		{
			E typedValue = (E)value;
			
			Object newValue = getReplacementValue(typedValue, row, column);
			super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
			
			setFontsAndAlignment();
			setCellColours(typedValue, isSelected);
			
			int rowHeight = getRowHeight();
			if (rowHeight > -1)
			{
				table.setRowHeight(rowHeight);
			}
			
			//For ButtonRenderer. If we're actually a component, then return the component itself (otherwise we just call toString()
			//on w/e object it is, which doesn't work)
			if (newValue instanceof Component)
			{
				return (Component)newValue;
			}
		}
		catch (ClassCastException cce)
		{
			Debug.stackTrace(cce, "Caught CCE rendering row [" + row + "], col [" + column + "]. Value [" + value + "]");
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		
		return this;
    }
	
	
	private Object getReplacementValue(E typedValue, int row, int column)
	{
		if (typedValue == null)
		{
			if (!allowNulls())
			{
				Debug.stackTrace("NULL element in table at row [" + row + "] and column [" + column + "].");
			}
			
			return typedValue;
		}
		
		return getReplacementValue(typedValue);
	}
	
	
	
	/**
	 * Default methods
	 */
	public boolean allowNulls()
	{
		return false;
	}
	public void setCellColours(E typedValue, boolean isSelected)
	{
		//do nothing
	}
	public void setFontsAndAlignment()
	{
		//do nothing
	}
	public int getRowHeight()
	{
		return -1;
	}
}
