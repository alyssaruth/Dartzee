package code.bean;

import java.awt.Color;

import bean.AbstractTableRenderer;
import code.utils.DartsColour;

public class X01ScoreRenderer extends AbstractTableRenderer<Integer>
{
	@Override
	public Object getReplacementValue(Integer object)
	{
		return object;
	}
	
	@Override
	public void setCellColours(Integer score, boolean isSelected)
	{
		Color fg = DartsColour.getScorerForegroundColour(score);
		Color bg = DartsColour.getScorerBackgroundColour(score);
		
		if (isSelected)
		{
			fg = Color.WHITE;
			bg = DartsColour.getDarkenedColour(bg);
		}
		
		setForeground(fg);
		setBackground(bg);
	}
}
