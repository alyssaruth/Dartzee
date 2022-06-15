package dartzee.core.util

interface IShuffler
{
    fun shuffleCollection(collection: MutableList<*>)
    fun <T> shuffleCollection(collection: List<T>): List<T>
}

class CollectionShuffler: IShuffler
{
    override fun shuffleCollection(collection: MutableList<*>)
    {
        collection.shuffle()
    }

    override fun <T> shuffleCollection(collection: List<T>) = collection.shuffled()
}