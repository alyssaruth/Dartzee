package burlton.core.code.util

fun <E> MutableList<E>.addUnique(element: E)
{
    if (!contains(element))
    {
        add(element)
    }
}

fun <E> MutableList<MutableList<E>>.flattenBatches(): MutableList<E>
{
    val ret = mutableListOf<E>()
    for (batch in this)
    {
        ret.addAll(batch)
    }

    return ret
}