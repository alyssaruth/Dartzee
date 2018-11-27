package burlton.dartzee.code.screen.ai;

import burlton.dartzee.code.ai.AbstractDartsModel;
import net.miginfocom.swing.MigLayout;

public class AIConfigurationGolf extends AbstractAIConfigurationSubPanel
{
	public AIConfigurationGolf()
	{
		setLayout(new MigLayout("", "[grow]", "[grow][grow][grow]"));
		
		add(panelDartOne, "cell 0 0,grow");
		add(panelDartTwo, "cell 0 1,grow");
		add(panelDartThree, "cell 0 2,grow");
		
	}
	
	private final AIConfigurationGolfDartPanel panelDartOne = new AIConfigurationGolfDartPanel(1);
	private final AIConfigurationGolfDartPanel panelDartTwo = new AIConfigurationGolfDartPanel(2);
	private final AIConfigurationGolfDartPanel panelDartThree = new AIConfigurationGolfDartPanel(3);
	
	@Override
	public boolean valid()
	{
		return true;
	}

	@Override
	public void populateModel(AbstractDartsModel model)
	{
		panelDartOne.populateModel(model);
		panelDartTwo.populateModel(model);
		panelDartThree.populateModel(model);
	}

	@Override
	public void initialiseFromModel(AbstractDartsModel model)
	{
		panelDartOne.initialiseFromModel(model);
		panelDartTwo.initialiseFromModel(model);
		panelDartThree.initialiseFromModel(model);

	}

}
