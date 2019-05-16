package burlton.core.code.obj

import burlton.core.code.util.MathsUtil
import java.util.ArrayList
import java.util.HashMap
import kotlin.Comparator



class HashMapCount<K>: HashMap<K, Int>()
{
    fun getTotalCount() : Int
    {
        return values.stream().mapToInt{v -> v}.sum()
    }

    /**
     * TODO - Remove once 100% kotlin
     */
    fun getKeysAsVector() : List<K>
    {
        return keys.toList()
    }

    @JvmOverloads
    fun incrementCount(key: K, amount: Int = 1): Int
    {
        val newVal = getOrDefault(key, 0) + amount
        this[key] = newVal

        return newVal
    }

    fun getCount(key: K): Int
    {
        return get(key) ?: return 0
    }

    /**
     * These ONLY WORK FOR INTEGER KEYS
     */
    fun calculateAverage(): Double
    {
        var totalValue = 0.0

        entries.forEach{
            totalValue += (it.key as Int * it.value)
        }

        val avg = totalValue / getTotalCount()
        return MathsUtil.round(avg, 1)
    }

    /**
     * Returns {1, 1, 1, 1, 1, 2, 2} from {1 -> 5, 2 -> 2}
     */
    fun getFlattenedOrderedList(comparator: Comparator<K>?): List<K>
    {
        val ret = mutableListOf<K>()

        val list = ArrayList<Map.Entry<K, Int>>(entries)
        if (comparator != null)
        {
            val entryComparator = Comparator<Map.Entry<K, Int>>{e1, e2 -> comparator.compare(e1.key, e2.key)}
            list.sortWith(entryComparator)
        }

        list.forEach{
            for (i in 0 until it.value)
            {
                ret.add(it.key)
            }
        }

        return ret
    }
}
