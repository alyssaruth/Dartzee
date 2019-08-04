package burlton.dartzee.test.db

import burlton.dartzee.code.db.PlayerImageEntity

class TestPlayerImageEntity: AbstractEntityTest<PlayerImageEntity>()
{
    override fun factoryDao() = PlayerImageEntity()

    override fun setExtraValuesForBulkInsert(e: PlayerImageEntity)
    {
        e.blobData = getBlobValue("Dennis")
    }
}