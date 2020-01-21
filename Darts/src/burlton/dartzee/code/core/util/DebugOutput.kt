package burlton.dartzee.code.core.util

interface DebugOutput
{
    fun getLogs(): String
    fun append(text: String)
    fun clear()
}
