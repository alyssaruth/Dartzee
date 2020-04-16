package dartzee.utils

import dartzee.core.util.DebugExtension
import dartzee.core.util.DialogUtil

class DartsDebugExtension : DebugExtension
{
    override fun exceptionCaught(showError: Boolean)
    {
        if (showError)
        {
            DialogUtil.showErrorLater("A serious error has occurred. Logs will now be sent for investigation." + "\nThere is no need to send a bug report.")
        }
    }
}