package dartzee.core.helper

import dartzee.core.util.IShuffler

/** Puts the first element to the end, i.e. [1, 2, 3, 4] -> [2, 3, 4, 1] */
class DeterministicCollectionShuffler : IShuffler {
    override fun <T> shuffleCollection(collection: List<T>) =
        collection.subList(1, collection.size) + collection[0]
}
