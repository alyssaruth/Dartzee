package burlton.dartzee.code.core.obj


class HashMapList<K: Comparable<K>, V> : HashMap<K, MutableList<V>>()
{
    fun getFlattenedValuesSortedByKey(): List<V>
    {
        val sortedEntries = entries.sortedBy { it.key }

        val values = mutableListOf<V>()
        sortedEntries.forEach {
            values.addAll(it.value)
        }

        return values
    }

    fun getAllValues(): MutableList<V>
    {
        return values.flatten().toMutableList()
    }

    fun putInList(key: K, value: V)
    {
        var list: MutableList<V>? = get(key)
        if (list == null)
        {
            list = mutableListOf()
            put(key, list)
        }

        list.add(value)
    }
}
