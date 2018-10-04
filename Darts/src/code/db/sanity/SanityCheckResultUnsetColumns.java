package code.db.sanity;

import java.util.ArrayList;

import code.db.AbstractEntity;

public class SanityCheckResultUnsetColumns
		extends AbstractSanityCheckResultEntities
{
	private String columnName = "";
	
	public SanityCheckResultUnsetColumns(String columnName, ArrayList<? extends AbstractEntity<?>> entities)
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
