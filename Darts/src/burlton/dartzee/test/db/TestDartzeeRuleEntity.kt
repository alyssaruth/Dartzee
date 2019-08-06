package burlton.dartzee.test.db

import burlton.dartzee.code.dartzee.DartzeeDartRuleEven
import burlton.dartzee.code.db.DartzeeRuleEntity
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeRuleEntity: AbstractEntityTest<DartzeeRuleEntity>()
{
    override fun factoryDao() = DartzeeRuleEntity()

    @Test
    fun `Should support NULL dartzee rules`()
    {
        val entity = DartzeeRuleEntity()
        val rowId = entity.assignRowId()
        entity.dart1Rule = DartzeeDartRuleEven()
        entity.dart2Rule = null

        entity.saveToDatabase()

        val reretrievedEntity = DartzeeRuleEntity().retrieveForId(rowId)!!
        reretrievedEntity.dart1Rule shouldBe DartzeeDartRuleEven()
        reretrievedEntity.dart2Rule shouldBe null
    }
}