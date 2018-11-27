package burlton.dartzee.code.bean;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

public abstract class GameParamFilterPanel extends JPanel
{
	public GameParamFilterPanel()
	{
		setLayout(new BorderLayout(0, 0));
	}
	
	public abstract String getGameParams();
	public abstract void setGameParams(String gameParams);
	public abstract String getFilterDesc();
	public abstract void enableChildren(boolean enabled);
	public abstract void addActionListener(ActionListener listener);
	
	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
	
		enableChildren(enabled);
	}
}
