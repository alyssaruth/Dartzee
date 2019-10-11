package burlton.dartzee.test.db

import burlton.dartzee.code.db.DartzeeTemplateEntity

class TestDartzeeTemplateEntity: AbstractEntityTest<DartzeeTemplateEntity>()
{
    override fun factoryDao() = DartzeeTemplateEntity()
}