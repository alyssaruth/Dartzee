package burlton.dartzee.code.screen.stats.player;

import burlton.core.code.obj.HashMapCount;

import java.util.ArrayList;

public final class HoleBreakdownWrapper
{
	private HashMapCount<Integer> hmScoreToCount = new HashMapCount<>();
	
	public void increment(int score)
	{
		hmScoreToCount.incrementCount(score);
	}
	public int getCount(int score)
	{
		return hmScoreToCount.getCount(score);
	}
	
	public double getAverage()
	{
		double totalGamesCounted = hmScoreToCount.getTotalCount();
		
		double weightedTotal = 0;
		ArrayList<Integer> scores = hmScoreToCount.getKeysAsVector();
		for (int score : scores)
		{
			int count = hmScoreToCount.getCount(score);
			weightedTotal += (score * count);
		}
		
		return weightedTotal / totalGamesCounted;
	}
	
	public Object[] getAsTableRow(Object holeIdentifier)
	{
		Object[] row = {holeIdentifier, getCount(1), getCount(2), getCount(3), getCount(4), getCount(5), getAverage()};
		return row;
	}
}