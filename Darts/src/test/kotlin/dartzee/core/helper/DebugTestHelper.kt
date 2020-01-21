package dartzee.test.core.helper

import dartzee.core.util.Debug

var checkedForExceptions = false

fun exceptionLogged(): Boolean
{
    checkedForExceptions = true
    return getLogs().contains("\tat")
}

fun getLogs(): String
{
    Debug.waitUntilLoggingFinished()
    return Debug.getCurrentLogs()
}

fun getLogLines(): List<String>
{
    val logs = getLogs()
    val lines = logs.lines().toMutableList()
    lines.removeAt(0)
    return lines
}