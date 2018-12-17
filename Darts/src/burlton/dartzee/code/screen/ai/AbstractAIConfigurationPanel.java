package burlton.dartzee.code.screen.ai;

import burlton.dartzee.code.ai.AbstractDartsModel;

/**
 * The panel that actually constructs the DartsModel, and thus contains the things that are specific to it
 */
public abstract class AbstractAIConfigurationPanel extends AbstractAIConfigurationSubPanel
{
	public abstract AbstractDartsModel initialiseModel();
	
	@Override
	public void populateModel(AbstractDartsModel model)
	{
		//Do nothing (we initialise the model instead)
	}
}
