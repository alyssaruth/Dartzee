package dartzee.core.util

fun <E> MutableList<E>.addUnique(element: E)
{
    if (!contains(element))
    {
        add(element)
    }
}

fun <K: Comparable<K>, V> Map<K, V>.getSortedValues(): List<V> = entries.sortedBy { it.key }.map { it.value }

fun List<Int>.minOrZero() = min() ?: 0
fun List<Int>.maxOrZero() = max() ?: 0

inline fun <T, R : Comparable<R>> Iterable<T>.sortedBy(descending: Boolean, crossinline selector: (T) -> R?): List<T> {
    return if (descending) this.sortedByDescending(selector) else this.sortedBy(selector)
}

fun IntRange.getDescription(): String
{
    return when
    {
        this.first == this.last -> "${this.first}"
        this.last == Integer.MAX_VALUE -> "${this.first}+"
        else -> "${this.first} - ${this.last}"
    }
}

fun <E> List<E>.getAllPermutations(): List<List<E>>
{
    if (size < 2)
    {
        return listOf(this)
    }

    if (size == 2)
    {
        return listOf(this, this.reversed())
    }

    val allPermutations = hashSetOf<List<E>>()
    forEachIndexed { ix, obj ->
        val subList = this.toMutableList()
        subList.removeAt(ix)

        for (permutation in subList.getAllPermutations())
        {
            allPermutations.add(listOf(obj) + permutation)
        }
    }

    return allPermutations.toList()
}

inline fun <T> Iterable<T>.allIndexed(predicate: (index: Int, T) -> Boolean): Boolean {
    if (this is Collection && isEmpty()) return true

    this.forEachIndexed { ix, it -> if (!predicate(ix, it)) return false }

    return true
}

fun <T> List<T>.getLongestStreak(isHit: (item: T) -> Boolean): List<T>
{
    var biggestChain = mutableListOf<T>()
    var currentChain = mutableListOf<T>()

    for (item in this)
    {
        if (!isHit(item))
        {
            currentChain = mutableListOf()
            continue
        }

        //It's a hit and we've reset for a new game if necessary. Just increment.
        currentChain.add(item)
        if (currentChain.size > biggestChain.size)
        {
            biggestChain = currentChain.toMutableList()
        }
    }

    return biggestChain
}