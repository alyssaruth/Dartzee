package burlton.dartzee.test.dartzee

import burlton.dartzee.code.dartzee.AbstractDartzeeRule
import io.kotlintest.matchers.string.shouldNotBeEmpty

abstract class AbstractConfigurableDartzeeRuleTest<E: AbstractDartzeeRule>: AbstractDartzeeRuleTest<E>()
{
    override fun `Validate empty rule`()
    {
        val rule = factory()
        rule.validate().shouldNotBeEmpty()
    }
}