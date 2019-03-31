package burlton.desktopcore.test.helpers

import burlton.core.code.util.DebugExtension
import burlton.desktopcore.code.util.DialogUtil

class TestDebugExtension: DebugExtension
{
    override fun exceptionCaught(showError: Boolean)
    {
        if (showError)
        {
            DialogUtil.showError("Exception")
        }
    }

    override fun unableToEmailLogs(){}
    override fun sendEmail(title: String?, message: String?){}
}