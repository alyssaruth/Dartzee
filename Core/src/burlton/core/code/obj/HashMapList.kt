package burlton.core.code.obj


class HashMapList<K: Comparable<K>, V> : SuperHashMap<K, MutableList<V>>()
{
    fun getValuesSize(): Int
    {
        var totalSize = 0
        val valueVectors = valuesAsVector
        for (valueVector in valueVectors)
        {
            totalSize += valueVector.size
        }

        return totalSize

    }

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
        val ret = mutableListOf<V>()

        val valueVectors = valuesAsVector
        for (valueVector in valueVectors)
        {
            ret.addAll(valueVector)
        }

        return ret
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
