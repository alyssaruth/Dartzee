package burlton.dartzee.test.db

import burlton.dartzee.code.db.PlayerEntity

class TestPlayerEntity: AbstractEntityTest<PlayerEntity>()
{
    override fun factoryDao() = PlayerEntity()
}