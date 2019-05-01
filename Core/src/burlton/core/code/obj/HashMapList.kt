package burlton.core.code.obj


class HashMapList<K: Comparable<K>, V> : HashMap<K, MutableList<V>>()
{
    /**
     * TODO - REMOVE (once fully over to Kotlin)
     */
    fun getAsHandyArrayList(key: K): HandyArrayList<V>?
    {
        val v = get(key)

        v ?: return null

        return HandyArrayList(v)
    }

    fun getFlattenedValuesSortedByKey(): List<V>
    {
        val sortedKeys = keys.toList().sorted()

        val values = mutableListOf<V>()
        sortedKeys.forEach{
            values.addAll(this[it]!!)
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
