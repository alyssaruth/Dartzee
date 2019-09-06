package burlton.dartzee.code.bean

import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.getAllDartRules

class DartzeeDartRuleSelector(desc: String): AbstractDartzeeRuleSelector<AbstractDartzeeDartRule>(desc)
{
    override fun getRules() = getAllDartRules()
}