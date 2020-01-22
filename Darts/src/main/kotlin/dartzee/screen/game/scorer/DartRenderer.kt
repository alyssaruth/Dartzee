package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import dartzee.`object`.DartHint
import dartzee.core.bean.AbstractTableRenderer
import java.awt.Color
import java.awt.Font

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

        font = Font(font.name, style, font.size)
    }

    override fun allowNulls(): Boolean
    {
        return true
    }
}