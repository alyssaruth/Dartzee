package burlton.dartzee.code.utils

import burlton.desktopcore.code.util.DebugExtension
import burlton.desktopcore.code.util.DialogUtil

class DartsDebugExtension : DebugExtension
{
    override fun exceptionCaught(showError: Boolean)
    {
        if (showError)
        {
            DialogUtil.showErrorLater("A serious error has occurred. Logs will now be sent for investigation." + "\nThere is no need to send a bug report.")
        }
    }

    override fun unableToEmailLogs()
    {
        DialogUtil.showErrorLater("An error occurred e-mailing logs. Please submit a bug report manually.")
    }

    override fun sendEmail(title: String, message: String)
    {
        ClientEmailer.sendClientEmail(title, message)
    }
}