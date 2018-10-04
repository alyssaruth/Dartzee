package code.screen.game;

import code.object.Dart;
import util.Debug;

public abstract class DartsScorer extends AbstractScorer
{		
	/**
	 * Add a dart to the scorer. 
	 */
	public void addDart(Dart drt)
	{
		int rowCount = model.getRowCount();
		if (shouldAddRow(rowCount))
		{
			Object[] row = getEmptyRow();
			addRow(row);
			rowCount++;
		}
		
		addDartToRow(rowCount - 1, drt);
	
		updatePlayerResult();
	}
	private boolean shouldAddRow(int rowCount)
	{
		if (rowCount == 0)
		{
			return true;
		}
		
		if (rowIsComplete(rowCount - 1))
		{
			return true;
		}
		
		return false;
	}
	private void addDartToRow(int rowNumber, Dart drt)
	{
		for (int i=0; i<getNumberOfColumnsForAddingNewDart(); i++)
		{
			Object currentVal = model.getValueAt(rowNumber, i);
			if (currentVal == null)
			{
				model.setValueAt(drt, rowNumber, i);
				repaint();
				return;
			}
		}
		
		Debug.stackTrace("Trying to add dart to row " + rowNumber + " but it's already full.");
	}
	
	/**
	 * Default Methods
	 */
	public void confirmCurrentRound()
	{
		
	}
	public void updatePlayerResult()
	{
		
	}
	
	/**
	 * Abstract Methods
	 */
	public abstract void clearCurrentRound();
	public abstract boolean rowIsComplete(int rowNumber);
	public abstract int getTotalScore();
}
