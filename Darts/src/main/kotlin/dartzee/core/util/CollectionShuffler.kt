package dartzee.core.util

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