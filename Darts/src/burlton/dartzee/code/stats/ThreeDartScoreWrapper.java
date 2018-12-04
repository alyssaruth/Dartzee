package burlton.dartzee.code.stats;

import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.obj.HashMapCount;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Wraps up the stuff for a specific 3 dart score
 */
public class ThreeDartScoreWrapper
{
	private HashMapCount<String> hmDartStrToCount = new HashMapCount<>();
	private HashMap<String, Long> hmDartStrToExampleGameId = new HashMap<>();
	
	public ThreeDartScoreWrapper()
	{
	}
	
	public void addDartStr(String dartStr, long gameId)
	{
		int count = hmDartStrToCount.incrementCount(dartStr);
		if (count == 1)
		{
			hmDartStrToExampleGameId.put(dartStr, gameId);
		}
	}
	
	public ArrayList<Object[]> getRows()
	{
		ArrayList<Object[]> rows = new ArrayList<>();
		
		HandyArrayList<String> allMethods = hmDartStrToCount.getKeysAsVector();
		for (String dartStr : allMethods)
		{
			int count = hmDartStrToCount.getCount(dartStr);
			long exampleGame = hmDartStrToExampleGameId.get(dartStr);
			
			Object[] row = {dartStr, count, exampleGame};
			rows.add(row);
		}
		
		return rows;
	}
	
	public int getTotalCount()
	{
		return hmDartStrToCount.getTotalCount();
	}
}
