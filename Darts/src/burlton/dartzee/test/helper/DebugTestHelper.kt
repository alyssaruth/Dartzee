package burlton.dartzee.test.helper

import burlton.core.code.util.Debug

fun exceptionLogged(): Boolean
{
    Debug.waitUntilLoggingFinished()
    return Debug.getLogs().contains("\tat")
}