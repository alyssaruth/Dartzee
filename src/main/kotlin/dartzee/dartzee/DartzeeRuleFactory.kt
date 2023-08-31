package dartzee.dartzee

import dartzee.screen.dartzee.DartzeeRuleCreationDialog

interface IDartzeeRuleFactory
{
    fun newRule(): DartzeeRuleDto?
    fun amendRule(rule: DartzeeRuleDto): DartzeeRuleDto
}

class DartzeeRuleFactory: IDartzeeRuleFactory
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