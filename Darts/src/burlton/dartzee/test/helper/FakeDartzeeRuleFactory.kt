package burlton.dartzee.test.helper

import burlton.dartzee.code.dartzee.AbstractDartzeeRuleFactory
import burlton.dartzee.code.dartzee.AbstractDartzeeTemplateFactory
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.db.DartzeeTemplateEntity

class FakeDartzeeRuleFactory(val ret: DartzeeRuleDto?): AbstractDartzeeRuleFactory()
{
    override fun newRule() = ret
    override fun amendRule(rule: DartzeeRuleDto) = ret ?: rule
}
class FakeDartzeeTemplateFactory(private val newTemplate: DartzeeTemplateEntity? = null, private val cancelCopy: Boolean = false): AbstractDartzeeTemplateFactory()
{
    override fun newTemplate() = newTemplate
    override fun copyTemplate(template: DartzeeTemplateEntity) =
            if (cancelCopy)
                null
            else {
                insertTemplateAndRule(name = template.name + " - Copy")
            }

}