package code.bean;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

public final class GameParamFilterPanelX01 extends GameParamFilterPanel
{
	public GameParamFilterPanelX01()
	{
		super();
		
		add(panel, BorderLayout.CENTER);
		panel.add(spinner);
	}
	
	private final JPanel panel = new JPanel();
	private final SpinnerX01 spinner = new SpinnerX01();

	@Override
	public String getGameParams()
	{
		return "" + spinner.getValue();
	}
	@Override
	public void setGameParams(String gameParams)
	{
		int x01 = Integer.parseInt(gameParams);
		spinner.setValue(x01);
	}
	
	@Override
	public String getFilterDesc() 
	{
		return "games of " + getGameParams();
	}

	@Override
	public void enableChildren(boolean enabled)
	{
		spinner.setEnabled(enabled);
	}
	
	@Override
	public void addActionListener(ActionListener listener)
	{
		spinner.addActionListener(listener);
	}
	
}
