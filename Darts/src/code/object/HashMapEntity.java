package code.object;

import java.util.HashMap;

import code.db.AbstractEntity;

public class HashMapEntity<K, V extends AbstractEntity<V>> extends HashMap<K, V>
{
	public long getRowId(K key)
	{
		V entity = get(key);
		return entity.getRowId();
	}
}
