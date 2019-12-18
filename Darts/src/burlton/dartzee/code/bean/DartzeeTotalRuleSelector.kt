package burlton.dartzee.code.bean

import burlton.dartzee.code.dartzee.getAllTotalRules
import burlton.dartzee.code.dartzee.total.AbstractDartzeeTotalRule
import burlton.desktopcore.code.util.enableChildren

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