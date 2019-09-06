package burlton.core.code.util

fun <E> MutableList<E>.addUnique(element: E)
{
    if (!contains(element))
    {
        add(element)
    }
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
    if (isEmpty())
    {
        return listOf(this)
    }

    val allPermutations = mutableListOf<List<E>>()
    forEachIndexed { ix, obj ->
        val subList = this.toMutableList()
        subList.removeAt(ix)

        for (permutation in subList.getAllPermutations())
        {
            allPermutations.add(listOf(obj) + permutation)
        }
    }

    return allPermutations
}