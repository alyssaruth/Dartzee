package burlton.dartzee.code.dartzee

import burlton.dartzee.code.db.DartzeeTemplateEntity
import burlton.dartzee.code.screen.dartzee.DartzeeTemplateDialog

abstract class AbstractDartzeeTemplateFactory
{
    abstract fun newTemplate(): DartzeeTemplateEntity?
    abstract fun copyTemplate(template: DartzeeTemplateEntity): DartzeeTemplateEntity?
}

class DartzeeTemplateFactory: AbstractDartzeeTemplateFactory()
{
    override fun newTemplate() = DartzeeTemplateDialog.createTemplate()
    override fun copyTemplate(template: DartzeeTemplateEntity) = DartzeeTemplateDialog.createTemplate(template)
}