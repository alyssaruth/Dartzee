package burlton.dartzee.test.dartzee

import burlton.dartzee.code.dartzee.dart.*
import burlton.dartzee.code.dartzee.generateRuleDescription
import burlton.dartzee.code.dartzee.getValidSegments
import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleGreaterThan
import burlton.dartzee.code.dartzee.total.DartzeeTotalRulePrime
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.test.borrowTestDartboard
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test

class TestDartzeeRuleDescriptions: AbstractDartsTest()
{
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

class TestValidSegments: AbstractDartsTest()
{
    @Test
    fun `getValidSegments should just filter by the ones that are valid`()
    {
        val rule = DartzeeRuleEntity()
        rule.dart1Rule = DartzeeDartRuleEven().toDbString()
        rule.dart2Rule = DartzeeDartRuleOdd().toDbString()
        rule.dart3Rule = DartzeeDartRuleEven().toDbString()
        rule.inOrder = true

        val dartboard = borrowTestDartboard()

        val segments = rule.getValidSegments(dartboard, listOf())

        segments.find { it.score == 20 } shouldNotBe null
        segments.find { it.score == 19 } shouldBe null
    }
}