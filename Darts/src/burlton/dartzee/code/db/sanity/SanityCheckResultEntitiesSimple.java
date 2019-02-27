package burlton.dartzee.code.db.sanity;

import burlton.dartzee.code.db.AbstractEntity;

import java.util.List;

public class SanityCheckResultEntitiesSimple extends AbstractSanityCheckResultEntities
{
	private String description = "";
	
	public SanityCheckResultEntitiesSimple(List<? extends AbstractEntity<?>> entities, String description)
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
