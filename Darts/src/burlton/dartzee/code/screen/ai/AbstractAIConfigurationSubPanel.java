package burlton.dartzee.code.screen.ai;

import javax.swing.JPanel;

import burlton.dartzee.code.ai.AbstractDartsModel;
import burlton.dartzee.code.ai.DartsModelNormalDistribution;

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
