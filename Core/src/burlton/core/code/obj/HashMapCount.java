package burlton.core.code.obj;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class HashMapCount<K> extends SuperHashMap<K, Integer>
{
	public int incrementCount(K key)
	{
		return incrementCount(key, 1);
	}
	public int incrementCount(K key, int amount)
	{
		Integer currentLong = get(key);
		if (currentLong == null)
		{
			currentLong = Integer.valueOf(0);
		}
		
		int val = currentLong.intValue() + amount;
		put(key, Integer.valueOf(val));
		return val;
	}
	public int getCount(K key)
	{
		Integer value = get(key);
		if (value == null)
		{
			return 0;
		}
		
		return value.intValue();
	}
	public int getTotalCount()
	{
		HandyArrayList<Integer> counts = getValuesAsVector();
		int total = 0;
		for (int count : counts)
		{
			total += count;
		}
		
		return total;
	}
	
	public K getKeyWithHighestCount()
	{
		Map.Entry<K, Integer> highest = getHighestEntry();
		return highest.getKey();
	}
	public int getHighestCount()
	{
		Map.Entry<K, Integer> highest = getHighestEntry();
		return highest.getValue();
	}
	private Map.Entry<K, Integer> getHighestEntry()
	{
		Set<Map.Entry<K, Integer>> set = entrySet();
		
		HandyArrayList<Map.Entry<K, Integer>> list = new HandyArrayList<>(set);
		list.sort(Comparator.comparingInt(e -> e.getValue()));

		return list.lastElement();
	}
	
	/**
	 * These ONLY WORK FOR INTEGER KEYS
	 */
	public double calculateAverage()
	{
		double totalValue = 0;
		
		Iterator<Map.Entry<K, Integer>> it = entrySet().iterator();
		for (; it.hasNext(); )
		{
			Map.Entry<K, Integer> entry = it.next();
			totalValue += (Integer)entry.getKey() * entry.getValue();
		}
		
		double avg = totalValue / getTotalCount();
		double roundedAvg = (double)Math.round(10 * avg) / 10;
		
		return roundedAvg;
	}
	
	/**
	 * Returns {1, 1, 1, 1, 1, 2, 2} from {1 -> 5, 2 -> 2}
	 */
	public ArrayList<K> getFlattenedOrderedList(Comparator<K> comparator)
	{
		ArrayList<K> ret = new ArrayList<>();
		
		Set<Map.Entry<K, Integer>> set = entrySet();
		
		HandyArrayList<Map.Entry<K, Integer>> list = new HandyArrayList<>(set);
		
		if (comparator != null)
		{
			list.sort((Map.Entry<K, Integer> m1, Map.Entry<K, Integer> m2) -> comparator.compare(m1.getKey(), m2.getKey()));
		}
		
		for (int i=0; i<list.size(); i++)
		{
			Map.Entry<K, Integer> entry = list.get(i);
			int numberToAdd = entry.getValue();
			K key = entry.getKey();
			for (int j=0; j<numberToAdd; j++)
			{
				ret.add(key);
			}
		}
		
		return ret;
	}
}
