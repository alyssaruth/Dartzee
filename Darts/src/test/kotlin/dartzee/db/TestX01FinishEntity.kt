package dartzee.db

import dartzee.db.X01FinishEntity

class TestX01FinishEntity: AbstractEntityTest<X01FinishEntity>()
{
    override fun factoryDao() = X01FinishEntity()
}