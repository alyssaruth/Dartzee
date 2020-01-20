package burlton.desktopcore.code.util

import burlton.dartzee.code.core.bean.IColourSelector
import burlton.desktopcore.code.screen.ColourChooserDialog

object InjectedDesktopCore
{
    var colourSelector: IColourSelector = ColourChooserDialog()
}