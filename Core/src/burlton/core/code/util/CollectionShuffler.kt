package burlton.core.code.util

interface IShuffler
{
    fun shuffleCollection(collection: MutableList<*>)
}

class CollectionShuffler: IShuffler
{
    override fun shuffleCollection(collection: MutableList<*>)
    {
        collection.shuffle()
    }
}