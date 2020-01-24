package dartzee.bean

import dartzee.core.util.enableChildren
import dartzee.dartzee.getAllTotalRules
import dartzee.dartzee.total.AbstractDartzeeTotalRule

class DartzeeTotalRuleSelector(desc: String): AbstractDartzeeRuleSelector<AbstractDartzeeTotalRule>(desc)
{
    override fun getRules() = getAllTotalRules()
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