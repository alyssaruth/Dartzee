package burlton.dartzee.code.db.sanity;

import java.util.ArrayList;

import burlton.dartzee.code.db.AbstractEntity;

public class SanityCheckResultHangingEntities extends AbstractSanityCheckResultEntities
{
	private String idColumn = "";
	
	public SanityCheckResultHangingEntities(String idColumn, ArrayList<? extends AbstractEntity<?>> entities)
	{
		super(entities);
		this.idColumn = idColumn;
	}
	
	@Override
	public String getDescription()
	{
		return getEntityName() + " rows where the " + idColumn + " points at a non-existent row";
	}

}
