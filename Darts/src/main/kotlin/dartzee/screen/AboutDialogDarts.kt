package dartzee.screen

import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.core.screen.AbstractAboutDialog

class AboutDialogDarts : AbstractAboutDialog()
{
    override fun getProductDesc() = "Darts $DARTS_VERSION_NUMBER"
    override fun getChangeLog() = ChangeLog()
}
