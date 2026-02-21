package dartzee.core.obj

class HashMapCount<K> : HashMap<K, Int>() {
    fun getTotalCount() = values.sum()

    fun incrementCount(key: K, amount: Int = 1): Int {
        val newVal = getOrDefault(key, 0) + amount
        this[key] = newVal

        return newVal
    }

    fun getCount(key: K) = get(key) ?: 0

    /** Returns {1, 1, 1, 1, 1, 2, 2} from {1 -> 5, 2 -> 2} */
    fun getFlattenedOrderedList(comparator: Comparator<K>?): List<K> {
        val ret = mutableListOf<K>()

        val list = ArrayList<Map.Entry<K, Int>>(entries)
        if (comparator != null) {
            val entryComparator =
                Comparator<Map.Entry<K, Int>> { e1, e2 -> comparator.compare(e1.key, e2.key) }
            list.sortWith(entryComparator)
        }

        list.forEach { entry -> repeat(entry.value) { ret.add(entry.key) } }

        return ret
    }
}
