package burlton.core.test.helper

import burlton.core.code.util.Debug

fun exceptionLogged(): Boolean
{
    return getLogs().contains("\tat")
}

fun getLogs(): String
{
    Debug.waitUntilLoggingFinished()
    return Debug.getCurrentLogs()
}