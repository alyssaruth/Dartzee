package burlton.dartzee.test.db

import burlton.dartzee.code.dartzee.dart.*
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

    @Test
    fun `Should describe 'score' dart rules`()
    {
        val entity = DartzeeRuleEntity()
        val rule = DartzeeDartRuleScore()
        rule.score = 15

        entity.dart1Rule = rule.toDbString()

        entity.generateRuleDescription() shouldBe "Score 15"
    }

    @Test
    fun `Dart and total rules should be concatenated if both are present`()
    {
        val entity = DartzeeRuleEntity()
        entity.dart1Rule = DartzeeDartRuleEven().toDbString()
        entity.totalRule = DartzeeTotalRuleGreaterThan().toDbString()

        entity.generateRuleDescription() shouldBe "Score Even, Total > 20"
    }
}