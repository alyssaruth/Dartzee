package burlton.dartzee.code.screen.game.scorer

import burlton.desktopcore.code.bean.AbstractTableRenderer
import java.awt.Color
import java.awt.Font
import javax.swing.SwingConstants

class DartzeeScoreRenderer(val maxScore: Int): AbstractTableRenderer<Int>()
{
    override fun getReplacementValue(value: Int) = value

    override fun setCellColours(typedValue: Int?, isSelected: Boolean)
    {
        if (typedValue == null)
        {
            background = null
            foreground = null
            return
        }

        val percent = if (typedValue == 0) 0f else typedValue.toFloat() / maxScore
        background = Color.getHSBColor(0.5.toFloat(), percent, 1f)
    }

    override fun setFontsAndAlignment()
    {
        horizontalAlignment = SwingConstants.CENTER
        font = Font(font.name, Font.BOLD, 12)
    }

    override fun allowNulls() = true
}