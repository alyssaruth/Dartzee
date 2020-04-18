package dartzee.core.helper

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