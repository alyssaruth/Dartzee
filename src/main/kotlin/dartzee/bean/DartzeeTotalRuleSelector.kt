package dartzee.bean

import dartzee.core.util.enableChildren
import dartzee.dartzee.aggregate.AbstractDartzeeAggregateRule
import dartzee.dartzee.getAllAggregateRules
import dartzee.dartzee.total.AbstractDartzeeTotalRule

class DartzeeTotalRuleSelector(desc: String): AbstractDartzeeRuleSelector<AbstractDartzeeAggregateRule>(desc)
{
    override fun getRules() = getAllAggregateRules()
    override fun shouldBeEnabled() = cbDesc.isSelected
    override fun isOptional() = true

    override fun setEnabled(enabled: Boolean)
    {
        super.setEnabled(enabled)

        enableChildren(enabled)

        //Re-enable this if necessary
        cbDesc.isEnabled = true
    }
}