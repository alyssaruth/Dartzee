package burlton.dartzee.code.screen.game;

import burlton.core.code.util.Debug;
import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.utils.PreferenceUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Collection;

import static burlton.dartzee.code.utils.RegistryConstantsKt.PREFERENCES_DOUBLE_BG_BRIGHTNESS;
import static burlton.dartzee.code.utils.RegistryConstantsKt.PREFERENCES_DOUBLE_FG_BRIGHTNESS;

public class DartsScorerGolf extends DartsScorer
{
	private static final int ROUNDS_HALFWAY = 9;
	private static final int ROUNDS_FULL = 18;
	private static final int SCORE_COLUMN = 4;
	
	private int currentScore = 0;
	private int fudgeFactor = 0; //For when we're displaying only a back 9, we need to shift everything up
	private boolean showGameId = false;
	
	public DartsScorerGolf() 
	{
		
	}

	@Override
	public int getNumberOfColumns()
	{
		return 5 + (showGameId?1:0);
	}

	@Override
	public void initImpl(String gameParams)
	{
		for (int i=0; i<=SCORE_COLUMN; i++)
		{
			tableScores.setRenderer(i, new DartRenderer(showGameId));
		}
		
		if (showGameId)
		{
			tableScores.setLinkColumnIndex(tableScores.getColumnCount() - 1);
		}
	}

	@Override
	public Object[] getEmptyRow()
	{
		Object[] emptyRow = super.getEmptyRow();
		
		//Set the first column to be the round number
		int rowCount = model.getRowCount();
		emptyRow[0] = getTargetForRowNumber(rowCount);
		
		return emptyRow;
	}

	@Override
	public boolean rowIsComplete(int rowNumber)
	{
		return model.getValueAt(rowNumber, SCORE_COLUMN) != null;
	}
	
	public void setTableForeground(Color color)
	{
		tableScores.setTableForeground(color);
		lblResult.setForeground(color);
	}
	
	/**
	 * Helper to add a full round at a time, for when we're viewing stats or loading a game
	 */
	public void addDarts(Collection<Dart> darts)
	{
		addDarts(darts, -1);
	}
	public void addDarts(Collection<Dart> darts, long gameId)
	{
		for (Dart dart : darts)
		{
			addDart(dart);
		}
		
		if (gameId > -1)
		{
			int row = tableScores.getRowCount() - 1;
			int column = tableScores.getColumnCount() - 1;
			model.setValueAt(gameId, row, column);
		}
		
		finaliseRoundScore();
	}
	
	public void finaliseRoundScore()
	{
		int rowNumber = model.getRowCount() - 1;
		int target = getTargetForRowNumber(rowNumber);
		Dart drt = getLastDartThrown(rowNumber);
		
		int score = drt.getGolfScore(target);
		
		model.setValueAt(score, rowNumber, SCORE_COLUMN);
		
		if (target == ROUNDS_HALFWAY
		  || target == ROUNDS_FULL)
		{
			int totalSoFar = calculateTotalScore();
			Object[] totalRow = {null, null, null, null, totalSoFar};
			addRow(totalRow);
		}
		
		currentScore += score;
		lblResult.setText("" + currentScore);
		lblResult.setVisible(true);
	}
	
	private Dart getLastDartThrown(int rowNumber)
	{
		Dart ret = null;
		for (int i=1; i<SCORE_COLUMN; i++)
		{
			Dart drt = (Dart)model.getValueAt(rowNumber, i);
			if (drt != null)
			{
				ret = drt;
			}
		}
		
		return ret;
	}
	
	private int calculateTotalScore()
	{
		int total = 0;
		int startRow = 0;
		if (model.getRowCount() > ROUNDS_HALFWAY)
		{
			startRow = ROUNDS_HALFWAY + 1;
		}
		
		for (int i=startRow; i<model.getRowCount(); i++)
		{
			Integer totalInt = (Integer)model.getValueAt(i, SCORE_COLUMN);
			if (totalInt != null)
			{
				total += totalInt.intValue();
			}
		}
		
		return total;
	}
	
	@Override
	public int getTotalScore()
	{
		return currentScore;
	}
	public void setFudgeFactor(int fudgeFactor)
	{
		this.fudgeFactor = fudgeFactor;
	}
	public void setShowGameId(boolean showGameId)
	{
		this.showGameId = showGameId;
	}
	
	/**
	 * Static methods
	 */
	private int getTargetForRowNumber(int row)
	{
		if (row < ROUNDS_HALFWAY)
		{
			//Row 0 is 1, etc.
			return row + fudgeFactor + 1;
		}
		
		if (row > ROUNDS_HALFWAY)
		{
			//We have an extra subtotal row to consider
			return row + fudgeFactor;
		}
		
		Debug.stackTrace("Trying to get round target for the subtotal row");
		return -1;
	}
	
	public static boolean isScoreRow(int row)
	{
		return row == ROUNDS_HALFWAY
		  || row == ROUNDS_FULL + 1;
	}
	
	/**
	 * Inner Classes
	 */
	private static class DartRenderer extends DefaultTableCellRenderer
	{
		private boolean showGameId = false;
		
		public DartRenderer(boolean showGameId)
		{
			this.showGameId = showGameId;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Object newValue = getReplacementValue(table, value, row);
			JComponent cell = (JComponent)super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
			
			setHorizontalAlignment(SwingConstants.CENTER);
    		setFont(new Font("Trebuchet MS", Font.BOLD, 15));
    		
    		Border border = getBorderForCell(row, column);
    		cell.setBorder(border);
    		
    		if (column == 0
    		  || newValue == null
    		  || isScoreRow(row))
    		{
    			setForeground(null);
    			setBackground(null);
    		}
    		else
    		{
    			int score = (int)newValue;
    			
    			double bgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_BG_BRIGHTNESS);
        		double fgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_FG_BRIGHTNESS);
        		
        		setForeground(getScorerColour(score, fgBrightness));
        		setBackground(getScorerColour(score, bgBrightness));
    		}
    		
    		return this;
		}
		
		public Object getReplacementValue(JTable table, Object obj, int row)
		{
			if (obj == null)
			{
				return null;
			}
			
			if (!(obj instanceof Dart))
			{
				return obj;
			}
			
			Dart drt = (Dart)obj;
			int target = (int)table.getValueAt(row, 0);
			int score = drt.getGolfScore(target);
			
			return score;
		}
		
		private Border getBorderForCell(int row, int col)
		{
			int top = 0;
			int bottom = 0;
			int left = 0;
			int right = 0;
			
			if (isScoreRow(row))
			{
				top = 2;
				bottom = 2;
			}
			
			if (col == 1)
			{
				left = 2;
			}
			
			if (col == 3)
			{
				right = 2;
			}
			
			if (showGameId
			  && col == 4)
			{
				right = 2;
			}
			
			return new MatteBorder(top, left, bottom, right, Color.BLACK);
		}
	}
	public static Color getScorerColour(int score, double brightness)
	{
		float hue = 0;
		if (score == 4)
		{
			hue = (float)0.1;
		}
		else if (score == 3)
		{
			hue = (float)0.2;
		}
		else if (score == 2)
		{
			hue = (float)0.3;
		}
		else if (score == 1)
		{
			hue = (float)0.5;
		}
		
		return Color.getHSBColor(hue, 1, (float)brightness);
	}
	
}
