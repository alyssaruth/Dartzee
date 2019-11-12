package burlton.dartzee.test.db

import burlton.dartzee.code.db.RoundDetailEntity

class TestRoundDetailEntity: AbstractEntityTest<RoundDetailEntity>()
{
    override fun factoryDao() = RoundDetailEntity()
}