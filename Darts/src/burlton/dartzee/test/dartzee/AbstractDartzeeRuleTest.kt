package burlton.dartzee.test.dartzee

import burlton.dartzee.code.dartzee.AbstractDartzeeRule
import burlton.dartzee.code.dartzee.parseDartRule
import burlton.dartzee.test.helper.AbstractDartsTest
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

        val parsedRule = parseDartRule(tag)!!
        parsedRule.getRuleIdentifier() shouldBe rule.getRuleIdentifier()
    }

    @Test
    open fun `Validate empty rule`()
    {
        val rule = factory()
        rule.validate().isEmpty() shouldBe emptyIsValid
    }
}