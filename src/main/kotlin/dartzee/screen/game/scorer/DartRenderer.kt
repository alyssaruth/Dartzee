package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import dartzee.`object`.DartHint
import dartzee.core.bean.AbstractTableRenderer
import dartzee.core.bean.enableAntiAliasing
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Font
import java.awt.Graphics

class DartRenderer : AbstractTableRenderer<Dart>()
{
    override fun getReplacementValue(value: Dart): Any
    {
        return when(value)
        {
            is DartHint -> "($value)"
            else -> "$value"
        }
    }

    override fun paint(g: Graphics?)
    {
        enableAntiAliasing(g)
        super.paint(g)
    }

    override fun setCellColours(typedValue: Dart?, isSelected: Boolean)
    {
        foreground = if (isSelected)
        {
            if (typedValue is DartHint) Color.CYAN else Color.WHITE
        }
        else
        {
            if (typedValue is DartHint) Color.RED else Color.BLACK
        }

        val style = if (typedValue is DartHint) Font.ITALIC else Font.PLAIN
        font = ResourceCache.BASE_FONT.deriveFont(style, font.size.toFloat())
    }

    override fun allowNulls(): Boolean
    {
        return true
    }
}