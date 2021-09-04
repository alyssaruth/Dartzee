package dartzee.screen.dartzee

import dartzee.core.bean.AbstractTableRenderer
import dartzee.dartzee.DartzeeRuleDto
import java.awt.Font

class DartzeeRuleRenderer(private val colNo: Int) : AbstractTableRenderer<DartzeeRuleDto>()
{
    override fun getReplacementValue(value: DartzeeRuleDto): Any
    {
        val ruleDesc = value.ruleName ?: value.generateRuleDescription()
        return if (colNo == 0) ruleDesc else value.getDifficultyDesc()
    }

    override fun setCellColours(typedValue: DartzeeRuleDto?, isSelected: Boolean)
    {
        foreground = typedValue?.calculationResult?.getForeground()
        background = typedValue?.calculationResult?.getBackground()
    }

    override fun setFontsAndAlignment()
    {
        font = Font(font.name, Font.PLAIN, 20)
    }
}