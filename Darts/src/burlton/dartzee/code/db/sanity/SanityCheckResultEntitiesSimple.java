package burlton.dartzee.code.db.sanity;

import java.util.ArrayList;

import burlton.dartzee.code.db.AbstractEntity;

public class SanityCheckResultEntitiesSimple extends AbstractSanityCheckResultEntities
{
	private String description = "";
	
	public SanityCheckResultEntitiesSimple(ArrayList<? extends AbstractEntity<?>> entities, String description)
	{
		super(entities);

		this.description = description;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

}
