package code.bean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import code.db.AbstractEntity;
import util.Debug;

public class TableModelEntity extends DefaultTableModel
{
	public TableModelEntity(ArrayList<? extends AbstractEntity<?>> entities)	
	{
		//Use the first entity to set up columns
		AbstractEntity<?> entity = entities.get(0);
		ArrayList<String> cols = entity.getColumns();
		for (String col : cols)
		{
			addColumn(col);
		}
		
		
		//Now create the rows
		addRows(entities, cols);
	}
	private void addRows(ArrayList<? extends AbstractEntity<?>> entities, ArrayList<String> columns)
	{
		try
		{
			for (AbstractEntity<?> entity : entities)
			{
				Object[] row = new Object[columns.size()];
				for (int i=0; i<columns.size(); i++)
				{
					String getMethod = "get" + columns.get(i);
					Class<AbstractEntity<?>> c = (Class<AbstractEntity<?>>)entity.getClass();
					Method m = c.getMethod(getMethod, new Class[0]);
					row[i] = m.invoke(entity, new Object[0]);
				}
				
				addRow(row);
			}
		}
		catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
		{
			Debug.stackTrace(e, "Reflection error displaying entities: " + entities);
		}
	}
}
