package burlton.dartzee.code.bean

import burlton.dartzee.code.utils.DartsColour
import burlton.desktopcore.code.bean.AbstractTableRenderer
import java.awt.Color

class X01ScoreRenderer : AbstractTableRenderer<Int>()
{
    override fun getReplacementValue(value: Int) = value

    override fun setCellColours(typedValue: Int?, isSelected: Boolean)
    {
        val score = typedValue!!
        var fg = DartsColour.getScorerForegroundColour(score.toDouble())
        var bg = DartsColour.getScorerBackgroundColour(score.toDouble())

        if (isSelected)
        {
            fg = Color.WHITE
            bg = DartsColour.getDarkenedColour(bg)
        }

        foreground = fg
        background = bg
    }
}
