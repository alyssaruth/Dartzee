package burlton.desktopcore.code.bean

import java.awt.Color

interface IColourSelector
{
    fun selectColour(initialColour: Color): Color
}