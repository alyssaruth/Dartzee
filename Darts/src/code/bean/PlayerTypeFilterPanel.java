package code.bean;

import javax.swing.JRadioButton;

import bean.RadioButtonPanel;
import util.Debug;

public class PlayerTypeFilterPanel extends RadioButtonPanel
{
	public PlayerTypeFilterPanel()
	{
		add(rdbtnAll);
		add(rdbtnHuman);
		add(rdbtnAi);
	}
	
	
	private final JRadioButton rdbtnAll = new JRadioButton("All");
	private final JRadioButton rdbtnHuman = new JRadioButton("Human");
	private final JRadioButton rdbtnAi = new JRadioButton("AI");
	
	public String getWhereSql()
	{
		if (rdbtnAll.isSelected())
		{
			return "";
		}
		else if (rdbtnHuman.isSelected())
		{
			return "Strategy = -1";
		}
		else if (rdbtnAi.isSelected())
		{
			return "Strategy > -1";
		}
		else
		{
			Debug.stackTrace("Unexpected setup - no radio buttons selected?");
			return "";
		}
	}
}
