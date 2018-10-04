package code.bean;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;

import bean.RadioButtonPanel;

public final class GameParamFilterPanelGolf extends GameParamFilterPanel
{
	public GameParamFilterPanelGolf()
	{
		super();
		
		add(panel, BorderLayout.CENTER);
		panel.add(rdbtn9);
		panel.add(rdbtn18);
		
		rdbtn18.setSelected(true); //Default to 18
	}
	
	private final RadioButtonPanel panel = new RadioButtonPanel();
	private final JRadioButton rdbtn9 = new JRadioButton("9 holes");
	private final JRadioButton rdbtn18 = new JRadioButton("18 holes");

	@Override
	public String getGameParams()
	{
		String selection = panel.getSelectionStr();
		return selection.replace(" holes", "");
	}
	@Override
	public void setGameParams(String gameParams)
	{
		if (gameParams.equals("9"))
		{
			rdbtn9.setSelected(true);
		}
		else
		{
			rdbtn18.setSelected(true);
		}
	}
	
	@Override
	public String getFilterDesc()
	{
		return "games of " + panel.getSelectionStr();
	}
	
	@Override
	public void enableChildren(boolean enabled)
	{
		rdbtn9.setEnabled(enabled);
		rdbtn18.setEnabled(enabled);
	}
	
	@Override
	public void addActionListener(ActionListener listener)
	{
		panel.addActionListener(listener);
	}
}
