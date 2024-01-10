package dartzee.bean

import dartzee.core.bean.AbstractTableRenderer
import dartzee.utils.DartsColour
import java.awt.Color

class X01ScoreRenderer : AbstractTableRenderer<Int>() {
    override fun getReplacementValue(value: Int) = value

    override fun setCellColours(typedValue: Int?, isSelected: Boolean) {
        val score = typedValue!!
        var fg = DartsColour.getScorerForegroundColour(score.toDouble())
        var bg = DartsColour.getScorerBackgroundColour(score.toDouble())

        if (isSelected) {
            fg = Color.WHITE
            bg = DartsColour.getDarkenedColour(bg)
        }

        foreground = fg
        background = bg
    }
}
