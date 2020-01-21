package dartzee.screen.game.scorer

import dartzee.dartzee.DartzeeRoundResult
import dartzee.utils.setColoursForDartzeeResult
import dartzee.core.bean.AbstractTableRenderer
import java.awt.Font
import javax.swing.SwingConstants

class DartzeeRoundResultRenderer : AbstractTableRenderer<DartzeeRoundResult>()
{
    override fun getReplacementValue(value: DartzeeRoundResult): Any
    {
        return when
        {
            value.ruleNumber == -1 -> "-"
            else -> "#${value.ruleNumber}"
        }
    }

    override fun setCellColours(typedValue: DartzeeRoundResult?, isSelected: Boolean)
    {
        if (typedValue == null)
        {
            background = null
            foreground = null
            return
        }

        setColoursForDartzeeResult(typedValue.success)
    }

    override fun setFontsAndAlignment()
    {
        horizontalAlignment = SwingConstants.CENTER
        font = Font(font.name, Font.BOLD, 12)
    }

    override fun allowNulls(): Boolean
    {
        return true
    }
}