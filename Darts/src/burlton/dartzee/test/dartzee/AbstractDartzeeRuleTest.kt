package burlton.dartzee.test.dartzee

import burlton.dartzee.code.dartzee.AbstractDartzeeRule
import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.getAllDartRules
import burlton.dartzee.code.dartzee.getAllTotalRules
import burlton.dartzee.code.dartzee.parseRule
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.Test

abstract class AbstractDartzeeRuleTest<E: AbstractDartzeeRule>: AbstractDartsTest()
{
    abstract fun factory(): E

    open val emptyIsValid = true

    @Test
    fun `Should be parsable from an atomic tag`()
    {
        val rule = factory()
        val tag = "<${rule.getRuleIdentifier()}/>"

        val parsedRule = parseRule(tag, getRuleList())!!
        parsedRule.getRuleIdentifier() shouldBe rule.getRuleIdentifier()
    }

    @Test
    open fun `Validate empty rule`()
    {
        val rule = factory()
        rule.validate().isEmpty() shouldBe emptyIsValid
    }

    @Test
    fun `Should be in the correct rule list`()
    {
        val rule = factory()
        getRuleList().filter { rule.javaClass.isInstance(it) } shouldHaveSize 1
    }

    private fun getRuleList(): List<AbstractDartzeeRule>
    {
        return if (factory() is AbstractDartzeeDartRule)
        {
            getAllDartRules()
        }
        else
        {
            getAllTotalRules()
        }
    }
}