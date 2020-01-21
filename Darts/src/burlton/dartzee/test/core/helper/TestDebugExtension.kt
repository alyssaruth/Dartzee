package burlton.dartzee.test.core.helper

import burlton.dartzee.code.core.util.DebugExtension
import burlton.dartzee.code.core.util.DialogUtil

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
    override fun sendEmail(title: String, message: String){}
}