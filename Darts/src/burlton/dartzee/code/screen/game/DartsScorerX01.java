package burlton.dartzee.code.screen.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import burlton.desktopcore.code.bean.AbstractTableRenderer;
import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.utils.DartsColour;

public class DartsScorerX01 extends DartsScorerPausable
{
	private static final int SCORE_COLUMN = 3;
	
	public DartsScorerX01()
	{
		super();
		
		lblStartingScore.setHorizontalAlignment(SwingConstants.CENTER);
		lblStartingScore.setFont(new Font("Trebuchet MS", Font.PLAIN, 16));
		panelNorth.add(lblStartingScore, BorderLayout.CENTER);
	}

	private final JLabel lblStartingScore = new JLabel("X01");
	
	
	@Override
	public void initImpl(String gameParams)
	{
		int startingScore = Integer.parseInt(gameParams);
		lblStartingScore.setText("" + startingScore);
		
		tableScores.getColumn(SCORE_COLUMN).setCellRenderer(new ScorerRenderer());
		for (int i=0; i<SCORE_COLUMN; i++)
		{
			tableScores.getColumn(i).setCellRenderer(new DartRenderer());
		}
	}
	
	@Override
	public boolean playerIsFinished()
	{
		return getLatestScoreRemaining() == 0;
	}
	
	public int getLatestScoreRemaining()
	{
		TableModel model = tableScores.getModel();
		
		int rowCount = model.getRowCount();
		if (rowCount == 0)
		{
			return Integer.parseInt(lblStartingScore.getText());
		}
		
		return (Integer)model.getValueAt(rowCount-1, SCORE_COLUMN);
	}
	
	/**
	 * How many darts have been thrown?
	 * 
	 * 3 * (rows - 1) + #(darts in the last row)
	 */
	@Override
	public int getTotalScore()
	{
		int rowCount = model.getRowCount();
		if (rowCount == 0)
		{
			return 0;
		}
		
		int dartCount = Math.max((model.getRowCount() - 1) * 3, 0);
		
		//We now use this mid-game
		if (rowIsComplete(rowCount - 1)
		  && !playerIsFinished())
		{
			return dartCount + 3;
		}
		
		for (int i=0; i<SCORE_COLUMN; i++)
		{
			Dart drt = (Dart)model.getValueAt(rowCount - 1, i);
			if (drt != null)
			{
				dartCount++;
			}
		}
		
		return dartCount;
	}
	
	@Override
	public boolean rowIsComplete(int rowNumber)
	{
		return model.getValueAt(rowNumber, SCORE_COLUMN) != null;
	}
	
	@Override
	public void clearCurrentRound()
	{
		int rowCount = model.getRowCount();
		if (rowCount == 0)
		{
			return;
		}
		
		//If we've come into here by clicking 'pause', the latest round might be a completed one.
		//Only clear the round if it's 'unconfirmed'.
		Object value = model.getValueAt(rowCount - 1, SCORE_COLUMN);
		if (value == null)
		{
			model.removeRow(rowCount - 1);
		}
	}
	
	@Override
	public int getNumberOfColumns()
	{
		return SCORE_COLUMN + 1;
	}
	
	public void finaliseRoundScore(int startingScore, boolean bust)
	{
		int row = model.getRowCount() - 1;
		if (!bust)
		{
			for (int i=0; i<SCORE_COLUMN; i++)
			{
				Dart dart = (Dart)model.getValueAt(row, i);
				if (dart != null)
				{
					startingScore = startingScore - dart.getTotal();
				}
			}
		}
		
		model.setValueAt(Integer.valueOf(startingScore), row, SCORE_COLUMN);
	}
	
	
	
	
	
	/**
	 * Static methods
	 */
	public static DartsScorerX01 factory(GamePanelX01 parent)
	{
		DartsScorerX01 scorer = new DartsScorerX01();
		scorer.setParent(parent);
		return scorer;
	}
	
	
	/**
	 * Inner classes
	 */
	private static class DartRenderer extends AbstractTableRenderer<Dart>
	{
		@Override
		public Object getReplacementValue(Dart drt)
		{
			if (drt == null)
			{
				return "";
			}
			
			return "" + drt;
		}
		
		@Override
		public boolean allowNulls()
		{
			return true;
		}
	}
	private static class ScorerRenderer extends DefaultTableCellRenderer
	{
        @Override
        public Component getTableCellRendererComponent(JTable table, Object
            value, boolean isSelected, boolean hasFocus, int row, int column) 
        {
    		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    		setHorizontalAlignment(SwingConstants.CENTER);
    		setFont(new Font("Trebuchet MS", Font.BOLD, 15));
    		int modelRow = table.convertRowIndexToModel(row);
    		
    		setColours(table, modelRow);
    		return this;
        }
        
        private void setColours(JTable table, int modelRow)
        {
        	TableModel tm = table.getModel();
        	int totalScore = getScoreAt(tm, modelRow, 0)
        				   + getScoreAt(tm, modelRow, 1)
        				   + getScoreAt(tm, modelRow, 2);
        	
        	Color fg = DartsColour.getScorerForegroundColour(totalScore);
        	Color bg = DartsColour.getScorerBackgroundColour(totalScore);
        	
        	setForeground(fg);
        	setBackground(bg);
        }
        
        private int getScoreAt(TableModel tm, int row, int col)
        {
        	Dart value = (Dart)tm.getValueAt(row, col);
        	if (value == null)
        	{
        		return 0;
        	}
        	
        	return value.getTotal();
        }
    }
}
