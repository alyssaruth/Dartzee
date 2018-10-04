package code.screen.ai;

import javax.swing.JPanel;

import code.ai.AbstractDartsModel;
import code.ai.DartsModelNormalDistribution;

/**
 * 
 * @author alexb
 *
 */
public abstract class AbstractAIConfigurationSubPanel extends JPanel
{
	public abstract boolean valid();
	public abstract void populateModel(AbstractDartsModel model);
	public abstract void initialiseFromModel(AbstractDartsModel model);
	
	public void reset()
	{
		initialiseFromModel(new DartsModelNormalDistribution());
	}
}
