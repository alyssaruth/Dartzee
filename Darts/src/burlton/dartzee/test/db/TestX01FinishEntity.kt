package burlton.dartzee.test.db

import burlton.dartzee.code.db.X01FinishEntity

class TestX01FinishEntity: AbstractEntityTest<X01FinishEntity>()
{
    override fun factoryDao() = X01FinishEntity()
}