package dartzee.test.db

import dartzee.db.PlayerImageEntity

class TestPlayerImageEntity: AbstractEntityTest<PlayerImageEntity>()
{
    override fun factoryDao() = PlayerImageEntity()

    override fun setExtraValuesForBulkInsert(e: PlayerImageEntity)
    {
        e.blobData = getBlobValue("Dennis")
    }
}