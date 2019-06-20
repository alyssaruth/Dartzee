package burlton.core.code.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Wrap up a hashmap to introduce extra helper methods
 */
public class SuperHashMap<K, V> extends HashMap<K, V>
{
	public SuperHashMap()
	{
		
	}
	
	public ArrayList<K> getKeysAsVector()
	{
		ArrayList<K> keys = new ArrayList<>();
		
		Iterator<K> it = keySet().iterator();
		for (; it.hasNext(); )
		{
			K key = it.next();
			keys.add(key);
		}
		
		return keys;
	}
	
	public ArrayList<V> getValuesAsVector()
	{
		return getValuesAsVector(false);
	}
	public ArrayList<V> getValuesAsVector(boolean distinct)
	{
		ArrayList<V> values = new ArrayList<>();
		
		Iterator<V> it = values().iterator();
		for (; it.hasNext(); )
		{
			V val = it.next();
			if (!distinct
			  || !values.contains(val))
			{
				values.add(val);
			}
		}
		
		return values;
	}
}
