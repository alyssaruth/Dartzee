package burlton.dartzee.code.bean;

import burlton.core.code.util.Debug;
import burlton.desktopcore.code.bean.RadioButtonPanel;

import javax.swing.*;

public class PlayerTypeFilterPanel extends RadioButtonPanel
{
	public PlayerTypeFilterPanel()
	{
		add(rdbtnAll);
		add(rdbtnHuman);
		add(rdbtnAi);
	}

	private final JRadioButton rdbtnAll = new JRadioButton("All");
	public final JRadioButton rdbtnHuman = new JRadioButton("Human");
	public final JRadioButton rdbtnAi = new JRadioButton("AI");
	
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
