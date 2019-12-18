package burlton.dartzee.code.screen.game.scorer;

import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.object.DartNotThrown;
import burlton.dartzee.code.utils.PreferenceUtil;
import burlton.dartzee.code.utils.RegistryConstantsKt;
import burlton.desktopcore.code.bean.AbstractTableRenderer;

import javax.swing.*;
import java.awt.*;

public final class DartsScorerRoundTheClock extends DartsScorerPausable
{
	private static final int BONUS_COLUMN = 3;
	
	private String clockType = "";
	
	//Always start at 1. Bit of an abuse to stick this here, it just avoids having another hmPlayerNumber->X.
	private int clockTarget = 1; 
	private int currentClockTarget = 1;
	
	@Override
	public void confirmCurrentRound()
	{
		clockTarget = currentClockTarget;
	}

	@Override
	public void clearRound(int roundNumber)
	{
		super.clearRound(roundNumber);
		currentClockTarget = clockTarget;
	}

	@Override
	public boolean playerIsFinished()
	{
		return clockTarget > 20;
	}
	
	@Override
	public int getTotalScore()
	{
		int rowCount = model.getRowCount();
		int dartCount = 0;
		
		for (int i=0; i<rowCount; i++)
		{
			for (int j=0; j<=BONUS_COLUMN; j++)
			{
				Dart drt = (Dart)model.getValueAt(i, j);
				if (drt != null
				  && !(drt instanceof DartNotThrown))
				{
					dartCount++;
				}
			}
		}
		
		return dartCount;
	}

	@Override
	public boolean rowIsComplete(int rowNumber)
	{
		return model.getValueAt(rowNumber, BONUS_COLUMN - 1) != null
		  && model.getValueAt(rowNumber, BONUS_COLUMN) != null;
	}

	@Override
	public int getNumberOfColumns()
	{
		return 4; //3 darts, plus bonus for hitting three consecutive
	}
	
	@Override
	public int getNumberOfColumnsForAddingNewDart()
	{
		return getNumberOfColumns(); //They're all for containing darts
	}

	@Override
	public void initImpl(String gameParams)
	{
		this.clockType = gameParams;
		
		for (int i=0; i<=BONUS_COLUMN; i++)
		{
			tableScores.getColumn(i).setCellRenderer(new DartRenderer());
		}
	}
	
	
	public int getCurrentClockTarget()
	{
		return currentClockTarget;
	}
	public void incrementCurrentClockTarget()
	{
		currentClockTarget++;
	}
	
	public void disableBrucey()
	{
		int row = model.getRowCount() - 1;
		model.setValueAt(new DartNotThrown(), row, BONUS_COLUMN);
	}
	
	private class DartRenderer extends AbstractTableRenderer<Dart>
	{	
		@Override
		public void setFontsAndAlignment()
		{
			setHorizontalAlignment(SwingConstants.CENTER);
    		setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		}
		
		@Override
		public void setCellColours(Dart typedValue, boolean isSelected)
		{
			if (typedValue == null)
			{
				setForeground(null);
				setBackground(null);
			}
			else if (typedValue instanceof DartNotThrown)
			{
				setForeground(Color.BLACK);
				setBackground(Color.BLACK);
			}
			else
			{
				double bgBrightness = PreferenceUtil.getDoubleValue(RegistryConstantsKt.PREFERENCES_DOUBLE_BG_BRIGHTNESS);
        		double fgBrightness = PreferenceUtil.getDoubleValue(RegistryConstantsKt.PREFERENCES_DOUBLE_FG_BRIGHTNESS);
        		
        		float hue = 0; //Red
        		if (typedValue.hitClockTarget(clockType))
        		{
        			hue = (float)0.3; //Green
        		}
        		
        		setForeground(Color.getHSBColor(hue, 1, (float)fgBrightness));
        		setBackground(Color.getHSBColor(hue, 1, (float)bgBrightness));
			}
		}
		
		@Override
		public Object getReplacementValue(Dart drt)
		{
			if (drt == null)
			{
				return null;
			}

			if (!drt.hitClockTarget(clockType))
			{
				return "X";
			}
			
			return "" + drt;
		}
		
		@Override
		public boolean allowNulls()
		{
			return true;
		}
	}
}
