package burlton.desktopcore.test.helper

import burlton.desktopcore.code.util.IShuffler

class FakeCollectionShuffler: IShuffler
{
    override fun shuffleCollection(collection: MutableList<*>)
    {
        collection.reverse()
    }
}