package burlton.dartzee.test.db

import burlton.dartzee.code.db.DartzeeRuleEntity

class TestDartzeeRuleEntity: AbstractEntityTest<DartzeeRuleEntity>()
{
    override fun factoryDao() = DartzeeRuleEntity()
}