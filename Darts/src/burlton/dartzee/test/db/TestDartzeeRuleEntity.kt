package burlton.dartzee.test.db

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleInner
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOdd
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOuter
import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleGreaterThan
import burlton.dartzee.code.dartzee.total.DartzeeTotalRulePrime
import burlton.dartzee.code.db.DartzeeRuleEntity
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeRuleEntity: AbstractEntityTest<DartzeeRuleEntity>()
{
    override fun factoryDao() = DartzeeRuleEntity()

    @Test
    fun `Should support unset dartzee rules`()
    {
        val entity = DartzeeRuleEntity()
        val rowId = entity.assignRowId()
        entity.dart1Rule = DartzeeDartRuleEven().toDbString()
        entity.dart2Rule = ""

        entity.saveToDatabase()

        val reretrievedEntity = DartzeeRuleEntity().retrieveForId(rowId)!!
        reretrievedEntity.dart1Rule shouldBe DartzeeDartRuleEven().toDbString()
        reretrievedEntity.dart2Rule shouldBe ""
    }

    @Test
    fun `Should describe an empty rule`()
    {
        val entity = DartzeeRuleEntity()
        entity.generateRuleDescription() shouldBe ""
    }

    @Test
    fun `Should describe total rules correctly`()
    {
        val entity = DartzeeRuleEntity()
        val totalRule = DartzeeTotalRulePrime()
        entity.totalRule = totalRule.toDbString()

        entity.generateRuleDescription() shouldBe "Total is prime"

        entity.totalRule = DartzeeTotalRuleGreaterThan().toDbString()
        entity.generateRuleDescription() shouldBe "Total > 20"
    }

    @Test
    fun `Should describe in-order dart rules`()
    {
        val entity = DartzeeRuleEntity()
        entity.dart1Rule = DartzeeDartRuleEven().toDbString()
        entity.dart2Rule = DartzeeDartRuleOdd().toDbString()
        entity.dart3Rule = DartzeeDartRuleEven().toDbString()
        entity.inOrder = true

        entity.generateRuleDescription() shouldBe "Even → Odd → Even"
    }

    @Test
    fun `Should condense the same rules if order isn't required`()
    {
        val entity = DartzeeRuleEntity()
        entity.dart1Rule = DartzeeDartRuleInner().toDbString()
        entity.dart2Rule = DartzeeDartRuleOuter().toDbString()
        entity.dart3Rule = DartzeeDartRuleOuter().toDbString()
        entity.inOrder = false

        entity.generateRuleDescription() shouldBe "{ 2x Outer, 1x Inner }"
    }
}