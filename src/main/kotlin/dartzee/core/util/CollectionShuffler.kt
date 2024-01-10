package dartzee.core.util

interface IShuffler {
    fun <T> shuffleCollection(collection: List<T>): List<T>
}

class CollectionShuffler : IShuffler {
    override fun <T> shuffleCollection(collection: List<T>) = collection.shuffled()
}
