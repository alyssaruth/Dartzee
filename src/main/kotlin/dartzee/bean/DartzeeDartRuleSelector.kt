package dartzee.bean

import dartzee.dartzee.dart.AbstractDartzeeDartRule
import dartzee.dartzee.getAllDartRules

class DartzeeDartRuleSelector(desc: String): AbstractDartzeeRuleSelector<AbstractDartzeeDartRule>(desc)
{
    override fun getRules() = getAllDartRules()
}