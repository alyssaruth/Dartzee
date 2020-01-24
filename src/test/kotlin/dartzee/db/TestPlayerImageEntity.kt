package dartzee.db

class TestPlayerImageEntity: AbstractEntityTest<PlayerImageEntity>()
{
    override fun factoryDao() = PlayerImageEntity()

    override fun setExtraValuesForBulkInsert(e: PlayerImageEntity)
    {
        e.blobData = getBlobValue("Dennis")
    }
}