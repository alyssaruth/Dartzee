package burlton.desktopcore.code.util

import burlton.desktopcore.code.bean.IColourSelector
import burlton.desktopcore.code.screen.ColourChooserDialog

object InjectedDesktopCore
{
    var colourSelector: IColourSelector = ColourChooserDialog()
}