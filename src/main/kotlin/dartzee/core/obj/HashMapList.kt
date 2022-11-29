package dartzee.core.obj


class HashMapList<K: Comparable<K>, V> : HashMap<K, MutableList<V>>()
{
    fun getFlattenedValuesSortedByKey() = entries.sortedBy { it.key }.flatMap { it.value }

    fun getAllValues() = values.flatten().toList()

    fun putInList(key: K, value: V)
    {
        val list = getOrPut(key, ::mutableListOf)
        list.add(value)
    }
}
