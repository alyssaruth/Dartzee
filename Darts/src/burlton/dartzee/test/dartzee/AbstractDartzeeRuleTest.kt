package burlton.dartzee.test.dartzee

import burlton.dartzee.code.dartzee.AbstractDartzeeRule
import burlton.dartzee.code.dartzee.parseDartRule
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe

abstract class AbstractDartzeeRuleTest<E: AbstractDartzeeRule>: AbstractDartsTest()
{
    abstract fun factory(): E

    fun `Should be parsable from an atomic tag`()
    {
        val rule = factory()
        val tag = "<${rule.getRuleIdentifier()}/>"

        val parsedRule = parseDartRule(tag)!!
        parsedRule.getRuleIdentifier() shouldBe rule.getRuleIdentifier()
    }
}