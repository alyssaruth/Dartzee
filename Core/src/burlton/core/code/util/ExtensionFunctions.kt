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