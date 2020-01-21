package dartzee.core.bean

import java.awt.Color

interface IColourSelector
{
    fun selectColour(initialColour: Color): Color
}