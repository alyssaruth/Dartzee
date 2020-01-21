package dartzee.screen.dartzee

import dartzee.dartzee.DartzeeRuleDto
import dartzee.core.bean.AbstractTableRenderer
import java.awt.Font

class DartzeeRuleRenderer(private val colNo: Int) : AbstractTableRenderer<DartzeeRuleDto>()
{
    override fun getReplacementValue(value: DartzeeRuleDto): Any
    {
        return if (colNo == 0) value.generateRuleDescription() else value.getDifficultyDesc()
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