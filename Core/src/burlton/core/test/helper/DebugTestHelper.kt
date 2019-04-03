package burlton.core.test.helper

import burlton.core.code.util.Debug

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