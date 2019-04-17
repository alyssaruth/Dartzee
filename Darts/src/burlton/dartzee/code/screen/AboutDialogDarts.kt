package burlton.dartzee.code.screen

import burlton.core.code.util.OnlineConstants
import burlton.desktopcore.code.screen.AbstractAboutDialog

class AboutDialogDarts : AbstractAboutDialog()
{
    override fun getProductDesc() = "Darts ${OnlineConstants.DARTS_VERSION_NUMBER}"
    override fun getChangeLog() = ChangeLog()
}
