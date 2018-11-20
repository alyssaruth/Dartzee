package code.screen.game;

import bean.AbstractTableRenderer;
import code.db.DartsMatchEntity;
import code.db.ParticipantEntity;
import code.utils.DartsColour;

/**
 * For the 'Match Summary' tab.
 */
public final class MatchScorer extends AbstractScorer
{
	private static final int COLUMN_NO_GAME_ID = 0;
	private static final int COLUMN_NO_FINAL_SCORE = 1;
	private static final int COLUMN_NO_FINISHING_POSITION = 2;
	private static final int COLUMN_NO_MATCH_POINTS = 3;
	
	private DartsMatchEntity match = null;
	
	/**
	 * Game #, Score, Position, Points
	 */
	@Override public int getNumberOfColumns()
	{
		return 4;
	}
	
	@Override
	public void initImpl(String gameParams)
	{
		tableScores.setGameColumnIndex(0);
		
		for (int i=COLUMN_NO_GAME_ID + 1; i<model.getColumnCount(); i++)
		{
			tableScores.getColumn(i).setCellRenderer(new ParticipantRenderer(i));
		}
		
		tableScores.setColumnWidths("100");
	}
	
	public void setMatch(DartsMatchEntity match)
	{
		this.match = match;
	}
	
	public void updateResult()
	{
		int totalScore = 0;
		
		int rowCount = tableScores.getRowCount();
		for (int i=0; i<rowCount; i++)
		{
			ParticipantEntity pt = (ParticipantEntity)tableScores.getValueAt(i, COLUMN_NO_MATCH_POINTS);
			totalScore += match.getScoreForFinishingPosition(pt.getFinishingPosition());
		}
		
		lblResult.setVisible(true);
		lblResult.setText("" + totalScore);
		
		//Also update the screen
		tableScores.repaint();
		
		if (match.isComplete())
		{
			//updateResultColourForPosition(rowCount);
		}
	}
	
	/**
	 * Inner classes
	 */
	private final class ParticipantRenderer extends AbstractTableRenderer<ParticipantEntity>
	{
		private int colNo = -1;
		
		public ParticipantRenderer(int colNo)
		{
			this.colNo = colNo;
		}
		
		@Override
		public Object getReplacementValue(ParticipantEntity pt)
		{
			if (colNo == COLUMN_NO_FINAL_SCORE)
			{
				int score = pt.getFinalScore();
				if (score == -1)
				{
					return "N/A";
				}
				
				return score;
			}
			else if (colNo == COLUMN_NO_FINISHING_POSITION)
			{
				return pt.getFinishingPositionDesc();
			}
			else if (colNo == COLUMN_NO_MATCH_POINTS)
			{
				return match.getScoreForFinishingPosition(pt.getFinishingPosition());
			}
			else
			{
				return "";
			}
		}
		
		@Override
		public void setCellColours(ParticipantEntity typedValue, boolean isSelected)
		{
			if (colNo == COLUMN_NO_FINISHING_POSITION)
			{
				int finishingPos = typedValue.getFinishingPosition();
				DartsColour.setFgAndBgColoursForPosition(this, finishingPos);
		
			}
		}
	}
}
