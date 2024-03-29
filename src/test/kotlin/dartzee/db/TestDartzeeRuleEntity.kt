package dartzee.db

import dartzee.dartzee.aggregate.DartzeeTotalRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleCustom
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.dartzee.dart.DartzeeDartRuleOuter
import dartzee.doubleNineteen
import dartzee.helper.insertDartzeeRule
import dartzee.helper.insertDartzeeTemplate
import dartzee.helper.insertGame
import dartzee.helper.makeDartzeeRuleCalculationResult
import dartzee.utils.getAllPossibleSegments
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class TestDartzeeRuleEntity : AbstractEntityTest<DartzeeRuleEntity>() {
    override fun factoryDao() = DartzeeRuleEntity()

    @Test
    fun `Should support unset dartzee rules`() {
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
    fun `Should support large custom rules in all the dart rule fields`() {
        val entity = DartzeeRuleEntity()
        val rowId = entity.assignRowId()

        val customRule = DartzeeDartRuleCustom()
        customRule.segments.addAll(getAllPossibleSegments())

        entity.dart1Rule = customRule.toDbString()
        entity.dart2Rule = customRule.toDbString()
        entity.dart3Rule = customRule.toDbString()

        shouldNotThrowAny { entity.saveToDatabase() }

        // Re-retrieve to make sure no silent truncation has happened
        val reretrievedEntity = DartzeeRuleEntity().retrieveForId(rowId)!!
        reretrievedEntity.dart1Rule shouldBe customRule.toDbString()
        reretrievedEntity.dart2Rule shouldBe customRule.toDbString()
        reretrievedEntity.dart3Rule shouldBe customRule.toDbString()
    }

    @Test
    fun `Should repopulate the DTO correctly`() {
        val entity = DartzeeRuleEntity()
        entity.dart1Rule = "<Even />"
        entity.dart2Rule = "<Odd />"
        entity.dart3Rule = "<Outer />"
        entity.aggregateRule = "<Even />"
        entity.allowMisses = true
        entity.inOrder = false
        entity.ruleName = "My Rule"

        val calculationResult = makeDartzeeRuleCalculationResult(listOf(doubleNineteen))
        entity.calculationResult = calculationResult.toDbString()

        val dto = entity.toDto()

        dto.dart1Rule!!.shouldBeInstanceOf<DartzeeDartRuleEven>()
        dto.dart2Rule!!.shouldBeInstanceOf<DartzeeDartRuleOdd>()
        dto.dart3Rule!!.shouldBeInstanceOf<DartzeeDartRuleOuter>()
        dto.aggregateRule!!.shouldBeInstanceOf<DartzeeTotalRuleEven>()
        dto.allowMisses shouldBe true
        dto.inOrder shouldBe false
        dto.ruleName shouldBe "My Rule"

        val newCalcResult = dto.calculationResult!!
        newCalcResult.scoringSegments.shouldContainExactly(doubleNineteen)
    }

    @Test
    fun `Should optionally skip populating the calculationResult when converting to a DTO`() {
        val entity = DartzeeRuleEntity()
        entity.dart1Rule = "<Even />"
        entity.dart2Rule = "<Odd />"
        entity.dart3Rule = "<Outer />"
        entity.aggregateRule = "<Even />"
        entity.allowMisses = true
        entity.inOrder = false

        val calculationResult = makeDartzeeRuleCalculationResult(listOf(doubleNineteen))
        entity.calculationResult = calculationResult.toDbString()

        val dto = entity.toDto(includeCalculationResult = false)

        dto.dart1Rule!!.shouldBeInstanceOf<DartzeeDartRuleEven>()
        dto.dart2Rule!!.shouldBeInstanceOf<DartzeeDartRuleOdd>()
        dto.dart3Rule!!.shouldBeInstanceOf<DartzeeDartRuleOuter>()
        dto.aggregateRule!!.shouldBeInstanceOf<DartzeeTotalRuleEven>()
        dto.allowMisses shouldBe true
        dto.inOrder shouldBe false

        dto.calculationResult shouldBe null
    }

    @Test
    fun `Should convert empty values to nulls when converting to a DTO`() {
        val entity = DartzeeRuleEntity()
        entity.dart1Rule = ""
        entity.dart2Rule = ""
        entity.dart3Rule = ""
        entity.aggregateRule = ""
        entity.ruleName = ""

        val dto = entity.toDto(includeCalculationResult = false)

        dto.dart1Rule.shouldBeNull()
        dto.dart2Rule.shouldBeNull()
        dto.dart3Rule.shouldBeNull()
        dto.aggregateRule.shouldBeNull()
        dto.ruleName.shouldBeNull()

        dto.calculationResult shouldBe null
    }

    @Test
    fun `Should retrieve rows for a template, sorted by ordinal`() {
        val templateA = insertDartzeeTemplate(name = "Template A")
        val templateB = insertDartzeeTemplate(name = "Template B")

        val ruleA3 =
            insertDartzeeRule(
                entityName = EntityName.DartzeeTemplate,
                entityId = templateA.rowId,
                ordinal = 3
            )
        val ruleA1 =
            insertDartzeeRule(
                entityName = EntityName.DartzeeTemplate,
                entityId = templateA.rowId,
                ordinal = 1
            )
        val ruleA2 =
            insertDartzeeRule(
                entityName = EntityName.DartzeeTemplate,
                entityId = templateA.rowId,
                ordinal = 2
            )

        insertDartzeeRule(
            entityName = EntityName.DartzeeTemplate,
            entityId = templateB.rowId,
            ordinal = 1
        )
        insertDartzeeRule(entityName = EntityName.Game, entityId = templateA.rowId, ordinal = 4)

        val rules = DartzeeRuleEntity().retrieveForTemplate(templateA.rowId)

        rules.map { it.rowId }.shouldContainExactly(ruleA1.rowId, ruleA2.rowId, ruleA3.rowId)
    }

    @Test
    fun `Should delete rows for a template`() {
        val templateA = insertDartzeeTemplate(name = "Template A")
        val templateB = insertDartzeeTemplate(name = "Template B")

        insertDartzeeRule(
            entityName = EntityName.DartzeeTemplate,
            entityId = templateA.rowId,
            ordinal = 3
        )
        insertDartzeeRule(
            entityName = EntityName.DartzeeTemplate,
            entityId = templateA.rowId,
            ordinal = 1
        )
        insertDartzeeRule(
            entityName = EntityName.DartzeeTemplate,
            entityId = templateA.rowId,
            ordinal = 2
        )

        val ruleB1 =
            insertDartzeeRule(
                entityName = EntityName.DartzeeTemplate,
                entityId = templateB.rowId,
                ordinal = 1
            )
        val gameRule =
            insertDartzeeRule(entityName = EntityName.Game, entityId = templateA.rowId, ordinal = 4)

        DartzeeRuleEntity().deleteForTemplate(templateA.rowId)

        val remainingRules = DartzeeRuleEntity().retrieveEntities()

        remainingRules.map { it.rowId }.shouldContainExactlyInAnyOrder(ruleB1.rowId, gameRule.rowId)
    }

    @Test
    fun `Should delete rows for a game`() {
        val gameA = insertGame()
        val gameB = insertGame()

        insertDartzeeRule(entityName = EntityName.Game, entityId = gameA.rowId, ordinal = 3)
        insertDartzeeRule(entityName = EntityName.Game, entityId = gameA.rowId, ordinal = 1)
        insertDartzeeRule(entityName = EntityName.Game, entityId = gameA.rowId, ordinal = 2)

        val ruleB1 =
            insertDartzeeRule(entityName = EntityName.Game, entityId = gameB.rowId, ordinal = 1)
        val templateRule =
            insertDartzeeRule(
                entityName = EntityName.DartzeeTemplate,
                entityId = insertDartzeeTemplate().rowId,
                ordinal = 1
            )

        DartzeeRuleEntity().deleteForGame(gameA.rowId)

        val remainingRules = DartzeeRuleEntity().retrieveEntities()

        remainingRules
            .map { it.rowId }
            .shouldContainExactlyInAnyOrder(ruleB1.rowId, templateRule.rowId)
    }

    @Test
    fun `Should retrieve rules for a game, sorted by ordinal`() {
        val gameA = insertGame()
        val gameB = insertGame()

        val ruleA3 =
            insertDartzeeRule(entityName = EntityName.Game, entityId = gameA.rowId, ordinal = 3)
        val ruleA1 =
            insertDartzeeRule(entityName = EntityName.Game, entityId = gameA.rowId, ordinal = 1)
        val ruleA2 =
            insertDartzeeRule(entityName = EntityName.Game, entityId = gameA.rowId, ordinal = 2)

        insertDartzeeRule(entityName = EntityName.Game, entityId = gameB.rowId, ordinal = 1)
        insertDartzeeRule(
            entityName = EntityName.DartzeeTemplate,
            entityId = gameA.rowId,
            ordinal = 4
        )

        val rules = DartzeeRuleEntity().retrieveForGame(gameA.rowId)

        rules.map { it.rowId }.shouldContainExactly(ruleA1.rowId, ruleA2.rowId, ruleA3.rowId)
    }
}
