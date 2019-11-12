package burlton.dartzee.test.db

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOdd
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOuter
import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleEven
import burlton.dartzee.code.db.DARTZEE_TEMPLATE
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.test.doubleNineteen
import burlton.dartzee.test.helper.*
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.types.shouldBeInstanceOf
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
    fun `Should repopulate the DTO correctly`()
    {
        val entity = DartzeeRuleEntity()
        entity.dart1Rule = "<Even />"
        entity.dart2Rule = "<Odd />"
        entity.dart3Rule = "<Outer />"
        entity.totalRule = "<Even />"
        entity.allowMisses = true
        entity.inOrder = false

        val calculationResult = makeDartzeeRuleCalculationResult(listOf(doubleNineteen))
        entity.calculationResult = calculationResult.toDbString()

        val dto = entity.toDto()

        dto.dart1Rule!!.shouldBeInstanceOf<DartzeeDartRuleEven>()
        dto.dart2Rule!!.shouldBeInstanceOf<DartzeeDartRuleOdd>()
        dto.dart3Rule!!.shouldBeInstanceOf<DartzeeDartRuleOuter>()
        dto.totalRule!!.shouldBeInstanceOf<DartzeeTotalRuleEven>()
        dto.allowMisses shouldBe true
        dto.inOrder shouldBe false

        val newCalcResult = dto.calculationResult!!
        newCalcResult.validSegments.shouldContainExactly(doubleNineteen)
    }

    @Test
    fun `Should retrieve rows for a template, sorted by ordinal`()
    {
        val templateA = insertDartzeeTemplate(name = "Template A")
        val templateB = insertDartzeeTemplate(name = "Template B")

        val ruleA3 = insertDartzeeRule(entityName = DARTZEE_TEMPLATE, entityId = templateA.rowId, ordinal = 3)
        val ruleA1 = insertDartzeeRule(entityName = DARTZEE_TEMPLATE, entityId = templateA.rowId, ordinal = 1)
        val ruleA2 = insertDartzeeRule(entityName = DARTZEE_TEMPLATE, entityId = templateA.rowId, ordinal = 2)

        insertDartzeeRule(entityName = DARTZEE_TEMPLATE, entityId = templateB.rowId, ordinal = 1)
        insertDartzeeRule(entityName = "Game", entityId = templateA.rowId, ordinal = 4)

        val rules = DartzeeRuleEntity().retrieveForTemplate(templateA.rowId)

        rules.map{ it.rowId }.shouldContainExactly(ruleA1.rowId, ruleA2.rowId, ruleA3.rowId)
    }

    @Test
    fun `Should delete rows for a template`()
    {
        val templateA = insertDartzeeTemplate(name = "Template A")
        val templateB = insertDartzeeTemplate(name = "Template B")

        insertDartzeeRule(entityName = DARTZEE_TEMPLATE, entityId = templateA.rowId, ordinal = 3)
        insertDartzeeRule(entityName = DARTZEE_TEMPLATE, entityId = templateA.rowId, ordinal = 1)
        insertDartzeeRule(entityName = DARTZEE_TEMPLATE, entityId = templateA.rowId, ordinal = 2)

        val ruleB1 = insertDartzeeRule(entityName = DARTZEE_TEMPLATE, entityId = templateB.rowId, ordinal = 1)
        val gameRule = insertDartzeeRule(entityName = "Game", entityId = templateA.rowId, ordinal = 4)

        DartzeeRuleEntity().deleteForTemplate(templateA.rowId)

        val remainingRules = DartzeeRuleEntity().retrieveEntities()

        remainingRules.map { it.rowId }.shouldContainExactlyInAnyOrder(ruleB1.rowId, gameRule.rowId)
    }

    @Test
    fun `Should retrieve rules for a game, sorted by ordinal`()
    {
        val gameA = insertGame()
        val gameB = insertGame()

        val ruleA3 = insertDartzeeRule(entityName = "Game", entityId = gameA.rowId, ordinal = 3)
        val ruleA1 = insertDartzeeRule(entityName = "Game", entityId = gameA.rowId, ordinal = 1)
        val ruleA2 = insertDartzeeRule(entityName = "Game", entityId = gameA.rowId, ordinal = 2)

        insertDartzeeRule(entityName = "Game", entityId = gameB.rowId, ordinal = 1)
        insertDartzeeRule(entityName = DARTZEE_TEMPLATE, entityId = gameA.rowId, ordinal = 4)

        val rules = DartzeeRuleEntity().retrieveForGame(gameA.rowId)

        rules.map{ it.rowId }.shouldContainExactly(ruleA1.rowId, ruleA2.rowId, ruleA3.rowId)
    }

}