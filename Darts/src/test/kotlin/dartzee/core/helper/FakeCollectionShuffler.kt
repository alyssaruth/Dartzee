package dartzee.test.core.helper

import dartzee.core.util.IShuffler

class FakeCollectionShuffler: IShuffler
{
    override fun shuffleCollection(collection: MutableList<*>)
    {
        collection.reverse()
    }
}