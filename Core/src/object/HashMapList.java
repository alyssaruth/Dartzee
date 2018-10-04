package object;


public class HashMapList<K, V> extends SuperHashMap<K, HandyArrayList<V>>
{
	public void putInList(K key, V value)
	{
		HandyArrayList<V> list = get(key);
		if (list == null)
		{
			list = new HandyArrayList<>();
			put(key, list);
		}
		
		list.add(value);
	}
	
	public int getValuesSize()
	{
		int totalSize = 0;
		HandyArrayList<HandyArrayList<V>> valueVectors = getValuesAsVector();
		for (HandyArrayList<V> valueVector : valueVectors)
		{
			totalSize += valueVector.size();
		}
		
		return totalSize;
		
	}
	
	public HandyArrayList<V> getAllValues()
	{
		HandyArrayList<V> ret = new HandyArrayList<>();
		
		HandyArrayList<HandyArrayList<V>> valueVectors = getValuesAsVector();
		for (HandyArrayList<V> valueVector : valueVectors)
		{
			ret.addAll(valueVector);
		}
		
		return ret;
	}
}
