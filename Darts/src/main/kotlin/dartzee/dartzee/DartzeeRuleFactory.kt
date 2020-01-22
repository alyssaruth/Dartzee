package dartzee.dartzee

import dartzee.screen.dartzee.DartzeeRuleCreationDialog

abstract class AbstractDartzeeRuleFactory
{
    abstract fun newRule(): DartzeeRuleDto?
    abstract fun amendRule(rule: DartzeeRuleDto): DartzeeRuleDto
}

class DartzeeRuleFactory: AbstractDartzeeRuleFactory()
{
    override fun newRule(): DartzeeRuleDto?
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.isVisible = true
        return dlg.dartzeeRule
    }

    override fun amendRule(rule: DartzeeRuleDto): DartzeeRuleDto
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.amendRule(rule)
        dlg.isVisible = true
        return dlg.dartzeeRule!!
    }
}