package burlton.dartzee.test.helper

import burlton.dartzee.code.dartzee.AbstractDartzeeRuleFactory
import burlton.dartzee.code.dartzee.DartzeeRuleDto

class FakeDartzeeRuleFactory(val ret: DartzeeRuleDto?): AbstractDartzeeRuleFactory()
{
    override fun newRule() = ret
    override fun amendRule(rule: DartzeeRuleDto) = ret ?: rule
}