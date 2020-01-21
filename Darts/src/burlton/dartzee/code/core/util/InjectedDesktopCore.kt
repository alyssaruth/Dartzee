package burlton.dartzee.code.core.util

import burlton.dartzee.code.core.bean.IColourSelector
import burlton.dartzee.code.core.screen.ColourChooserDialog

object InjectedDesktopCore
{
    var colourSelector: IColourSelector = ColourChooserDialog()
}