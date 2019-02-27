package burlton.dartzee.code.bean;

import burlton.core.code.util.Debug;
import burlton.dartzee.code.db.AbstractEntity;

import javax.swing.table.DefaultTableModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class TableModelEntity extends DefaultTableModel
{
	public TableModelEntity(List<? extends AbstractEntity<?>> entities)
	{
		//Use the first entity to set up columns
		AbstractEntity<?> entity = entities.get(0);
		List<String> cols = entity.getColumns();
		for (String col : cols)
		{
			addColumn(col);
		}
		
		
		//Now create the rows
		addRows(entities, cols);
	}
	private void addRows(List<? extends AbstractEntity<?>> entities, List<String> columns)
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
