package burlton.dartzee.code.bean;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;

import burlton.desktopcore.code.bean.RadioButtonPanel;
import burlton.dartzee.code.db.GameEntity;

public final class GameParamFilterPanelRoundTheClock
		extends GameParamFilterPanel
{
	public GameParamFilterPanelRoundTheClock()
	{
		super();
		
		add(panel, BorderLayout.CENTER);
		panel.add(rdbtnStandard);
		panel.add(rdbtnDoubles);
		panel.add(rdbtnTrebles);
	}
	
	private final RadioButtonPanel panel = new RadioButtonPanel();
	private final JRadioButton rdbtnStandard = new JRadioButton(GameEntity.CLOCK_TYPE_STANDARD);
	private final JRadioButton rdbtnDoubles = new JRadioButton(GameEntity.CLOCK_TYPE_DOUBLES);
	private final JRadioButton rdbtnTrebles = new JRadioButton(GameEntity.CLOCK_TYPE_TREBLES);

	@Override
	public String getGameParams()
	{
		return panel.getSelectionStr();
	}
	@Override
	public void setGameParams(String gameParams)
	{
		panel.setSelection(gameParams);
	}
	
	@Override
	public String getFilterDesc()
	{
		return getGameParams() + " games";
	}
	
	@Override
	public void enableChildren(boolean enabled)
	{
		rdbtnStandard.setEnabled(enabled);
		rdbtnDoubles.setEnabled(enabled);
		rdbtnTrebles.setEnabled(enabled);
	}
	
	@Override
	public void addActionListener(ActionListener listener)
	{
		panel.addActionListener(listener);
	}
}
