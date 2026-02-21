package dartzee.core.util

import dartzee.core.bean.IColourSelector
import dartzee.core.screen.ColourChooserDialog

object InjectedDesktopCore {
    var colourSelector: IColourSelector = ColourChooserDialog()
}
