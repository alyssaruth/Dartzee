package burlton.dartzee.test.core.helper

import burlton.dartzee.code.core.util.IShuffler

class FakeCollectionShuffler: IShuffler
{
    override fun shuffleCollection(collection: MutableList<*>)
    {
        collection.reverse()
    }
}