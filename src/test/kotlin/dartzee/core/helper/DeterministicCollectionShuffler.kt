package dartzee.core.helper

import dartzee.core.util.IShuffler

class DeterministicCollectionShuffler: IShuffler
{
    override fun shuffleCollection(collection: MutableList<*>)
    {
        collection.reverse()
    }

    /**
     * Put the first element to the end
     */
    override fun <T> shuffleCollection(collection: List<T>) = collection.subList(1, collection.size) + collection[0]
}