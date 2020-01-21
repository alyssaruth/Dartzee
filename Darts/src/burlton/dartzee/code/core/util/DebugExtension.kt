package burlton.dartzee.code.core.util

interface DebugExtension
{
    fun exceptionCaught(showError: Boolean)
    fun unableToEmailLogs()
    fun sendEmail(title: String, message: String)
}
