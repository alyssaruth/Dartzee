package burlton.core.test.helper

import burlton.core.code.util.IShuffler

class FakeCollectionShuffler: IShuffler
{
    override fun shuffleCollection(collection: MutableList<*>)
    {
        collection.reverse()
    }
}