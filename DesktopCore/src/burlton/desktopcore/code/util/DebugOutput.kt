package burlton.desktopcore.code.util

interface DebugOutput
{
    fun getLogs(): String
    fun append(text: String)
    fun clear()
}
