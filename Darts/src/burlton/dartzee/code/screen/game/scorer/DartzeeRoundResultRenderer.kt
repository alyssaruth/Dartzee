package burlton.dartzee.code.screen.game.scorer

import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.utils.setColoursForDartzeeResult
import burlton.desktopcore.code.bean.AbstractTableRenderer
import java.awt.Color

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

    override fun allowNulls(): Boolean
    {
        return true
    }
}