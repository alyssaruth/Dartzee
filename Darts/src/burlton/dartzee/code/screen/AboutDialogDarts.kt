package burlton.dartzee.code.screen

import burlton.dartzee.code.utils.DARTS_VERSION_NUMBER
import burlton.dartzee.code.core.screen.AbstractAboutDialog

class AboutDialogDarts : AbstractAboutDialog()
{
    override fun getProductDesc() = "Darts $DARTS_VERSION_NUMBER"
    override fun getChangeLog() = ChangeLog()
}
