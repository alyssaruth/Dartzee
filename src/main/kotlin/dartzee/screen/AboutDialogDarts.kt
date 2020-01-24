package dartzee.screen

import dartzee.core.screen.AbstractAboutDialog
import dartzee.utils.DARTS_VERSION_NUMBER

class AboutDialogDarts : AbstractAboutDialog()
{
    override fun getProductDesc() = "Darts $DARTS_VERSION_NUMBER"
    override fun getChangeLog() = ChangeLog()
}
