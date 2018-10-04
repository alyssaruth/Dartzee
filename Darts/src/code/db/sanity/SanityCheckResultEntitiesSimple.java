package code.db.sanity;

import java.util.ArrayList;

import code.db.AbstractEntity;

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
