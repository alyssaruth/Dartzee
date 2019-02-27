package burlton.dartzee.code.db.sanity;

import burlton.dartzee.code.db.AbstractEntity;

import java.util.List;

public class SanityCheckResultUnsetColumns
		extends AbstractSanityCheckResultEntities
{
	private String columnName = "";
	
	public SanityCheckResultUnsetColumns(String columnName, List<? extends AbstractEntity<?>> entities)
	{
		super(entities);
		
		this.columnName = columnName;
	}
	
	@Override
	public String getDescription()
	{
		return getEntityName() + " rows where " + columnName + " is unset";
	}

}
